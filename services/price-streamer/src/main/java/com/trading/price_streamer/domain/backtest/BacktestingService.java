package com.trading.price_streamer.domain.backtest;

import com.trading.price_streamer.domain.common.Signal;
import com.trading.price_streamer.domain.common.PriceTickEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service // Marks this class as a Spring Service
public class BacktestingService {

    /**
     * Calculates the Simple Moving Average (SMA) for a list of price ticks.
     *
     * @param priceTicks The list of historical price data.
     * @param period The number of ticks to include in the average (e.g., 10 for a 10-period SMA).
     * @return An array of doubles containing the calculated SMA values. The first (period - 1) values will be 0.
     */
    public double[] calculateSMA(List<PriceTickEntity> priceTicks, int period) {
        // We can't calculate an SMA if we don't have enough data
        if (priceTicks == null || priceTicks.size() < period) {
            return new double[0]; // Return an empty array
        }

        double[] smaValues = new double[priceTicks.size()];
        double sum = 0;

        // Calculate the sum for the first `period` of ticks
        for (int i = 0; i < period; i++) {
            sum += priceTicks.get(i).getPrice();
        }

        // The first SMA value
        smaValues[period - 1] = sum / period;

        // Calculate the rest of the SMAs using a more efficient "sliding window" approach
        for (int i = period; i < priceTicks.size(); i++) {
            // Add the new price and subtract the oldest price from the sum
            sum += priceTicks.get(i).getPrice() - priceTicks.get(i - period).getPrice();
            smaValues[i] = sum / period;
        }

        return smaValues;
    }
    /**
     * Generates trading signals based on the SMA crossover strategy.
     *
     * @param priceTicks The list of historical price data.
     * @param shortPeriod The period for the short-term SMA.
     * @param longPeriod The period for the long-term SMA.
     * @return A list of Signals (BUY, SELL, or HOLD).
     */
    public List<Signal> generateSmaCrossoverSignals(List<PriceTickEntity> priceTicks, int shortPeriod, int longPeriod) {
        List<Signal> signals = new ArrayList<>();
        if (priceTicks.size() < longPeriod) {
            return signals; // Not enough data to generate signals
        }

        double[] shortSma = calculateSMA(priceTicks, shortPeriod);
        double[] longSma = calculateSMA(priceTicks, longPeriod);

        for (int i = 1; i < priceTicks.size(); i++) {
            // A BUY signal is generated when the short SMA crosses ABOVE the long SMA
            if (shortSma[i] > longSma[i] && shortSma[i - 1] <= longSma[i - 1]) {
                signals.add(Signal.BUY);
            }
            // A SELL signal is generated when the short SMA crosses BELOW the long SMA
            else if (shortSma[i] < longSma[i] && shortSma[i - 1] >= longSma[i - 1]) {
                signals.add(Signal.SELL);
            }
            // Otherwise, we hold
            else {
                signals.add(Signal.HOLD);
            }
        }
        return signals;
    }
    /**
     * Runs a full backtest simulation for the SMA Crossover strategy.
     *
     * @param priceTicks The historical data.
     * @param shortPeriod The short SMA period.
     * @param longPeriod The long SMA period.
     * @param initialBalance The starting cash balance.
     * @return A BacktestResult object with the performance summary.
     */

    public BacktestResult runSmaCrossoverBacktest(List<PriceTickEntity> priceTicks, int shortPeriod, int longPeriod, double initialBalance) {
        if (priceTicks.size() < longPeriod) {
            return new BacktestResult("SMA Crossover", BigDecimal.valueOf(initialBalance), BigDecimal.valueOf(initialBalance), BigDecimal.ZERO, 0, 0);
        }

        List<Signal> signals = generateSmaCrossoverSignals(priceTicks, shortPeriod, longPeriod);
        BigDecimal cash = BigDecimal.valueOf(initialBalance);
        BigDecimal btcHoldings = BigDecimal.ZERO;
        int trades = 0;
        boolean isHolding = false; // Are we currently holding BTC?

        // We start iterating from longPeriod since signals are not reliable before that
        for (int i = longPeriod; i < priceTicks.size(); i++) {
            Signal currentSignal = signals.get(i - 1); // Align signal with the correct tick
            double currentPrice = priceTicks.get(i).getPrice();

            if (Signal.BUY == currentSignal && !isHolding) {
                // Buy with all available cash
                btcHoldings = cash.divide(BigDecimal.valueOf(currentPrice), 8, RoundingMode.DOWN);
                cash = BigDecimal.ZERO;
                isHolding = true;
                trades++;
                System.out.println("EXECUTED BUY @ " + currentPrice);
            } else if (Signal.SELL == currentSignal && isHolding) {
                // Sell all holdings
                cash = btcHoldings.multiply(BigDecimal.valueOf(currentPrice));
                btcHoldings = BigDecimal.ZERO;
                isHolding = false;
                trades++;
                System.out.println("EXECUTED SELL @ " + currentPrice);
            }
        }

        // At the end of the simulation, calculate the final portfolio value
        BigDecimal finalBalance = cash;
        if (isHolding) {
            double lastPrice = priceTicks.get(priceTicks.size() - 1).getPrice();
            finalBalance = btcHoldings.multiply(BigDecimal.valueOf(lastPrice));
        }

        BigDecimal pnl = finalBalance.subtract(BigDecimal.valueOf(initialBalance));
        double pnlPercentage = pnl.divide(BigDecimal.valueOf(initialBalance), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();

        return new BacktestResult(
                "SMA Crossover (" + shortPeriod + ", " + longPeriod + ")",
                BigDecimal.valueOf(initialBalance),
                finalBalance.setScale(2, RoundingMode.HALF_UP),
                pnl.setScale(2, RoundingMode.HALF_UP),
                pnlPercentage,
                trades
        );
    }


    }