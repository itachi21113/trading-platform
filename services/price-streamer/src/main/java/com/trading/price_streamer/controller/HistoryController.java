package com.trading.price_streamer.controller;

import com.trading.price_streamer.domain.common.Signal;
import com.trading.price_streamer.domain.common.PriceTickEntity;
import com.trading.price_streamer.domain.common.PriceTickRepository;
import com.trading.price_streamer.domain.backtest.BacktestingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController // Marks this class as a REST controller
@CrossOrigin
public class HistoryController {

    @Autowired
    private PriceTickRepository priceTickRepository;
    @Autowired
    private BacktestingService backtestingService;

    @GetMapping("/history") // Maps HTTP GET requests for /history to this method
    public List<PriceTickEntity> getHistory(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "range", defaultValue = "24h") String range) {

        Instant endTime = Instant.now();
        Instant startTime;

        // Simple logic to determine the start time based on the range parameter
        if ("24h".equalsIgnoreCase(range)) {
            startTime = endTime.minus(24, ChronoUnit.HOURS);
        } else if ("7d".equalsIgnoreCase(range)) {
            startTime = endTime.minus(7, ChronoUnit.DAYS);
        } else {
            // Default to 1 hour if the range is not recognized
            startTime = endTime.minus(1, ChronoUnit.HOURS);
        }

        // Call the repository to fetch the data
        return priceTickRepository.findBySymbolAndTimestampBetween(symbol, startTime, endTime);
    }
    @GetMapping("/test-signals")
    public List<Signal> testSignals() {
        Instant endTime = Instant.now();
        // Fetch a larger set of data to increase the chance of a crossover
        Instant startTime = endTime.minus(10, ChronoUnit.MINUTES);
        List<PriceTickEntity> recentTicks = priceTickRepository.findBySymbolAndTimestampBetween("BTC-USD", startTime, endTime);

        // Generate signals with a 10-period short SMA and a 30-period long SMA
        return backtestingService.generateSmaCrossoverSignals(recentTicks, 10, 30);
    }
}