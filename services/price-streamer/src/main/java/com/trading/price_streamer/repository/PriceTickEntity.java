package com.trading.price_streamer.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Entity // Marks this class as a JPA entity (a table in the DB)
@Table(name = "price_ticks") // Specifies the table name
@NoArgsConstructor
@AllArgsConstructor
public class PriceTickEntity {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increments the ID
    private Long id;

    @Column(nullable = false) // Ensures the column cannot be empty
    private String symbol;

    private double price;

    @Column(nullable = false, columnDefinition = "TIMESTAMPTZ") // Defines a timestamp with time zone
    private Instant timestamp;

    public PriceTickEntity(String symbol, double price, Instant timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }



}