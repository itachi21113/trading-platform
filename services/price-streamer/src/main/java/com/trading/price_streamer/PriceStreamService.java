package com.trading.price_streamer;

import com.trading.price_streamer.repository.PriceTickEntity;
import com.trading.price_streamer.repository.PriceTickRepository;
import com.trading.price_streamer.service.AlertService;
import com.trading.price_streamer.service.MLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- Import this
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;

@Service
public class PriceStreamService {

    // Inject the WebSocket messaging template
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PriceTickRepository priceTickRepository;

    @Autowired
    private AlertService alertService;

    @Autowired
    private MLService mlService;

    // A small in-memory buffer to hold the last 15 ticks
    private final LinkedList<PriceTickEntity> recentTicksBuffer = new LinkedList<>();
    private static final int BUFFER_SIZE = 15;


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

        // --- NEW: ML Prediction Logic ---
        // 1. Add the new tick to our buffer
        recentTicksBuffer.add(entity);
        if (recentTicksBuffer.size() > BUFFER_SIZE) {
            recentTicksBuffer.removeFirst(); // Keep the buffer from growing too large
        }

        // 2. If we have enough data, call the ML service
        if (recentTicksBuffer.size() >= 10) { // Model needs at least 10 ticks for features
            String prediction = mlService.getPrediction(new ArrayList<>(recentTicksBuffer));

            // 3. Broadcast the prediction over a new WebSocket topic
            messagingTemplate.convertAndSend("/topic/predictions", prediction);
            System.out.println("AI Prediction: " + prediction);
        }
    }
}