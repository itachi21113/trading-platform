package com.trading.price_streamer.service;

import com.trading.price_streamer.repository.PriceTickEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MLService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    public String getPrediction(List<PriceTickEntity> recentTicks) {
        // *** MODIFIED PART: Convert entities to the correct request format ***
        PredictionRequest request = new PredictionRequest(recentTicks);

        try {
            Map<String, String> response = restTemplate.postForObject(mlServiceUrl + "/predict", request, Map.class);
            return response != null ? response.get("prediction") : "N/A";
        } catch (Exception e) {
            System.err.println("Error calling ML service: " + e.getMessage());
            return "Error";
        }
    }

    // --- NEW, CORRECTED REQUEST STRUCTURE ---

    // A DTO representing a single tick with a String timestamp
    private static class MLPredictionTick {
        public String timestamp;
        public double price;

        public MLPredictionTick(PriceTickEntity entity) {
            this.timestamp = entity.getTimestamp().toString(); // Convert Instant to ISO 8601 String
            this.price = entity.getPrice();
        }
    }

    // The main request body containing a list of the new DTOs
    private static class PredictionRequest {
        public List<MLPredictionTick> ticks;

        public PredictionRequest(List<PriceTickEntity> entities) {
            this.ticks = entities.stream()
                    .map(MLPredictionTick::new)
                    .collect(Collectors.toList());
        }
    }
}