package com.trading.price_streamer.model;

import java.math.BigDecimal;

// Using Lombok for clean, boilerplate-free code
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResult {
    private String strategy;
    private BigDecimal initialBalance;
    private BigDecimal finalBalance;
    private BigDecimal profitOrLoss;
    private double profitPercentage;
    private int totalTrades;
}