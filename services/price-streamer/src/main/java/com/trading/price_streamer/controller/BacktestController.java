package com.trading.price_streamer.controller;

import com.trading.price_streamer.model.BacktestResult;
import com.trading.price_streamer.repository.PriceTickEntity;
import com.trading.price_streamer.repository.PriceTickRepository;
import com.trading.price_streamer.service.BacktestingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@CrossOrigin
public class BacktestController {

    @Autowired
    private PriceTickRepository priceTickRepository;

    @Autowired
    private BacktestingService backtestingService;

    @GetMapping("/backtest/sma-crossover")
    public BacktestResult runSmaBacktest(
            @RequestParam(defaultValue = "24h") String range,
            @RequestParam(defaultValue = "10") int shortPeriod,
            @RequestParam(defaultValue = "30") int longPeriod,
            @RequestParam(defaultValue = "10000") double initialBalance) {

        // 1. Fetch data from DB
        Instant endTime = Instant.now();
        Instant startTime;
        if ("7d".equalsIgnoreCase(range)) {
            startTime = endTime.minus(7, ChronoUnit.DAYS);
        } else {
            startTime = endTime.minus(24, ChronoUnit.HOURS);
        }
        List<PriceTickEntity> priceTicks = priceTickRepository.findBySymbolAndTimestampBetween("BTC-USD", startTime, endTime);

        // 2. Run the backtest
        return backtestingService.runSmaCrossoverBacktest(priceTicks, shortPeriod, longPeriod, initialBalance);
    }
}