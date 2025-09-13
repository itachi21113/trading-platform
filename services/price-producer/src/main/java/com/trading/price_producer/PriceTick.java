package com.trading.price_producer;

public record PriceTick(String symbol, double price, long timestamp) {
}