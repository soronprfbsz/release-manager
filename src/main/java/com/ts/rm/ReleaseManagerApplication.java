package com.ts.rm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ReleaseManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReleaseManagerApplication.class, args);
    }
}
