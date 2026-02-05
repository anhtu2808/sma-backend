package com.sma.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmaCoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmaCoreServiceApplication.class, args);
    }

}
