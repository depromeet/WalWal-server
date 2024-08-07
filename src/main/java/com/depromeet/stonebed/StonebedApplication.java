package com.depromeet.stonebed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StonebedApplication {
    public static void main(String[] args) {
        SpringApplication.run(StonebedApplication.class, args);
    }
}
