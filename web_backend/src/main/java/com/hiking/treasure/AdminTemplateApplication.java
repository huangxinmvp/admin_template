package com.hiking.treasure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.hiking.treasure")
@EnableScheduling
public class AdminTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminTemplateApplication.class, args);
    }

}
