package com.trading.price_streamer;

import com.trading.price_streamer.repository.PriceTickEntity;
import com.trading.price_streamer.repository.PriceTickRepository;
import com.trading.price_streamer.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- Import this
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PriceStreamService {

    // Inject the WebSocket messaging template
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PriceTickRepository priceTickRepository;

    @Autowired
    private AlertService alertService;


    @KafkaListener(topics = "price-ticks", groupId = "price-streamer-group")
    public void consume(PriceTick tick) {
        // 1. Convert the incoming PriceTick record to our PriceTickEntity
        PriceTickEntity entity = new PriceTickEntity(
                tick.symbol(),
                tick.price(),
                Instant.ofEpochMilli(tick.timestamp()) // Convert long timestamp to Instant
        );

        // 2. Use the repository to save the entity
        priceTickRepository.save(entity);
        System.out.println("Saved to DB: " + entity.getSymbol() + " @ " + entity.getPrice());

        // Print to the console (we can keep this for logging)
        System.out.println("Received price tick: " + tick);
        messagingTemplate.convertAndSend("/topic/prices", tick);
        alertService.checkAndTriggerAlerts(tick);
    }
}