package com.trading.price_streamer;

public record PriceTick(String symbol, double price, long timestamp) {
}