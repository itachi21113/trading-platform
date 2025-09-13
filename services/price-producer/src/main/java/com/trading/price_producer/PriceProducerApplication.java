package com.trading.price_producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PriceProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PriceProducerApplication.class, args);
	}

}
