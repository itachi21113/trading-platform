package com.trading.price_streamer.controller;

import com.trading.price_streamer.repository.PriceTickEntity;
import com.trading.price_streamer.repository.PriceTickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController // Marks this class as a REST controller
public class HistoryController {

    @Autowired
    private PriceTickRepository priceTickRepository;

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
}