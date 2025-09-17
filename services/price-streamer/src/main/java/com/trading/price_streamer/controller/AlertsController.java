package com.trading.price_streamer.controller;

import com.trading.price_streamer.model.CreateAlertRequest;
import com.trading.price_streamer.repository.AlertEntity;
import com.trading.price_streamer.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts") // Base path for all methods in this controller
@CrossOrigin
public class AlertsController {

    @Autowired
    private AlertService alertService;

    @PostMapping
    public ResponseEntity<AlertEntity> createAlert(
            @RequestBody CreateAlertRequest request,
            @AuthenticationPrincipal Jwt jwt) { // Spring Security injects the user's token

        String userId = jwt.getSubject(); // The 'sub' claim is the unique user ID from Auth0
        AlertEntity createdAlert = alertService.createAlert(request, userId);
        return ResponseEntity.ok(createdAlert);
    }

    @GetMapping
    public ResponseEntity<List<AlertEntity>> getMyAlerts(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<AlertEntity> alerts = alertService.getActiveAlertsForUser(userId);
        return ResponseEntity.ok(alerts);
    }
}