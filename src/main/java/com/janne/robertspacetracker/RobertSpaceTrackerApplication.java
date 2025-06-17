package com.janne.robertspacetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RobertSpaceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RobertSpaceTrackerApplication.class, args);
    }

}
