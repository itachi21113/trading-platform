package com.trading.price_streamer.repository;

import com.trading.price_streamer.model.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {
    // Spring Data JPA will automatically create the query from this method name
    List<AlertEntity> findByUserIdAndStatus(String userId, AlertStatus status);
    List<AlertEntity> findBySymbolAndStatus(String symbol, AlertStatus status);

}