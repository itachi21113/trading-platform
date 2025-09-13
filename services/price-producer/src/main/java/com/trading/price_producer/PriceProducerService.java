package com.trading.price_producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service // Mark this class as a Spring Service
public class PriceProducerService {

    // Spring will automatically create and configure this for us.
    // It's our tool for sending messages to Kafka.
    @Autowired
    private KafkaTemplate<String, PriceTick> kafkaTemplate;

    private static final String TOPIC = "price-ticks";
    private double lastBtcPrice = 60000.0; // Starting price for our fake data

    /**
     * This method will run automatically every 1000 milliseconds (1 second).
     * It uses a simple "random walk" algorithm to generate a new price.
     */
    @Scheduled(fixedRate = 1000)
    public void generateAndSendPrice() {
        // Generate a small random change
        double priceChange = ThreadLocalRandom.current().nextDouble(-100.0, 100.0);
        lastBtcPrice += priceChange;

        // Create a new PriceTick object using the record we made earlier
        PriceTick tick = new PriceTick("BTC-USD", lastBtcPrice, System.currentTimeMillis());

        // Use the kafkaTemplate to send the tick to our Kafka topic.
        // The topic name is "price-ticks".
        // The message key is the symbol "BTC-USD".
        // The message value is our 'tick' object, which Spring will convert to JSON.
        kafkaTemplate.send(TOPIC, tick.symbol(), tick);

        // Print to console so we can see it's working
        System.out.println("Sent price tick: " + tick);
    }
}