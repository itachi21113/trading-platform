package com.trading.price_streamer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Marks this as a Spring Data repository
public interface PriceTickRepository extends JpaRepository<PriceTickEntity, Long> {
    // By extending JpaRepository, we get methods like save(), findById(), findAll(), etc.
    // for our PriceTickEntity, which has a primary key of type Long.
    // We don't need to write any code here for now!
}