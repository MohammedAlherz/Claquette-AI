package com.example.claquetteai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClaquetteAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClaquetteAiApplication.class, args);
    }



}
