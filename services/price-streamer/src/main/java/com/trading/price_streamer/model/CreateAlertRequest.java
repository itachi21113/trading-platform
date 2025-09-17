package com.trading.price_streamer.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAlertRequest {
    private String symbol;
    private AlertCondition condition;
    private BigDecimal targetPrice;
}