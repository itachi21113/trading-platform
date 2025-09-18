package com.trading.price_streamer.domain.common;

public record PriceTick(String symbol, double price, long timestamp) {
}