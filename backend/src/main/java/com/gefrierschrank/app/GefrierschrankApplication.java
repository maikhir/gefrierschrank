package com.gefrierschrank.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GefrierschrankApplication {

    public static void main(String[] args) {
        SpringApplication.run(GefrierschrankApplication.class, args);
    }
}