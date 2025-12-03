package com.uade.ritmofitapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RitmoFitApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RitmoFitApiApplication.class, args);
	}

}
