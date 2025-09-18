package com.trading.price_streamer.domain.alert;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "user_id")
    private String userId; // This will store the unique ID from Auth0 (the 'sub' claim)

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertCondition alertCondition;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status = AlertStatus.ACTIVE; // Default new alerts to ACTIVE

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

}