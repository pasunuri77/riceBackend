package com.rice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RicebackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RicebackendApplication.class, args);
    }

}
