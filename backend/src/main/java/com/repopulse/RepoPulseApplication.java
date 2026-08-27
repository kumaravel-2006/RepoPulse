package com.repopulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {
    "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"
})
public class RepoPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoPulseApplication.class, args);
    }
}