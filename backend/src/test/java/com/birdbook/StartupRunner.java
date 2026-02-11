package com.birdbook;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.birdbook.service.BirdService;

@Component
public class StartupRunner implements CommandLineRunner {
 
    private final BirdService birdService;
 
    public StartupRunner(BirdService birdService) {
        this.birdService = birdService;
    }
 
    @Override
    public void run(String... args) {
        birdService.getAllBirds().forEach(System.out::println);
    }
}