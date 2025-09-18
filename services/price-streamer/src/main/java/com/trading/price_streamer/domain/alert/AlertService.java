package com.trading.price_streamer.domain.alert;

import com.trading.price_streamer.domain.common.PriceTick;
import com.trading.price_streamer.model.CreateAlertRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Inject the template


    /**
     * Creates a new alert for a given user.
     * @param request The alert details from the frontend.
     * @param userId The unique ID of the user from their Auth0 token.
     * @return The saved AlertEntity.
     */
    public AlertEntity createAlert(CreateAlertRequest request, String userId) {
        AlertEntity newAlert = new AlertEntity();
        newAlert.setUserId(userId);
        newAlert.setSymbol(request.getSymbol());
        newAlert.setAlertCondition(request.getCondition());
        newAlert.setTargetPrice(request.getTargetPrice());
        // Status and createdAt are set by default in the entity
        return alertRepository.save(newAlert);
    }

    /**
     * Finds all active alerts for a specific user.
     * @param userId The user's unique ID.
     * @return A list of the user's active alerts.
     */
    public List<AlertEntity> getActiveAlertsForUser(String userId) {
        // We will implement this query method in the repository next
        return alertRepository.findByUserIdAndStatus(userId, AlertStatus.ACTIVE);
    }

    public void checkAndTriggerAlerts(PriceTick tick) {
        // Find all active alerts for the symbol (e.g., "BTC-USD")
        List<AlertEntity> activeAlerts = alertRepository.findBySymbolAndStatus(tick.symbol(), AlertStatus.ACTIVE);

        for (AlertEntity alert : activeAlerts) {
            BigDecimal currentPrice = BigDecimal.valueOf(tick.price());
            boolean triggered = false;

            // Check if the condition is met
            if (alert.getAlertCondition() == AlertCondition.BELOW && currentPrice.compareTo(alert.getTargetPrice()) < 0) {
                triggered = true;
            } else if (alert.getAlertCondition() == AlertCondition.ABOVE && currentPrice.compareTo(alert.getTargetPrice()) > 0) {
                triggered = true;
            }

            if (triggered) {
                // 1. Update the alert status to TRIGGERED so it doesn't fire again
                alert.setStatus(AlertStatus.TRIGGERED);
                alertRepository.save(alert);

                // 2. Send a private notification to the specific user
                String message = String.format(
                        "ALERT: %s is now %s your target of $%,.2f. Current price: $%,.2f",
                        alert.getSymbol(),
                        alert.getAlertCondition().toString().toLowerCase(),
                        alert.getTargetPrice(),
                        currentPrice
                );

                // This sends the message to the user's private queue
                messagingTemplate.convertAndSendToUser(alert.getUserId(), "/queue/notifications", message);

                System.out.println("Triggered alert for user " + alert.getUserId());
            }
        }
    }

}