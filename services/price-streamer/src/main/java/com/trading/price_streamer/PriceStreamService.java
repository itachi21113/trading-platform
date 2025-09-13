package com.trading.price_streamer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- Import this
import org.springframework.stereotype.Service;

@Service
public class PriceStreamService {

    // Inject the WebSocket messaging template
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "price-ticks", groupId = "price-streamer-group")
    public void consume(PriceTick tick) {
        // Print to the console (we can keep this for logging)
        System.out.println("Received price tick: " + tick);

        // Send the received tick to the "/topic/prices" destination
        // Any client subscribed to this topic will receive the message.
        messagingTemplate.convertAndSend("/topic/prices", tick);
    }
}