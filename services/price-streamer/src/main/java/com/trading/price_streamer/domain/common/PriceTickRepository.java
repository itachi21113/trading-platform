package com.trading.price_streamer.domain.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository // Marks this as a Spring Data repository
public interface PriceTickRepository extends JpaRepository<PriceTickEntity, Long> {
    List<PriceTickEntity> findBySymbolAndTimestampBetween(String symbol, Instant start, Instant end);
}