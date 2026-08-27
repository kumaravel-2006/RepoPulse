package com.repopulse.controller;

import com.repopulse.dto.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    private static final Logger logger =
            LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/health")
    public HealthResponse health() {

        logger.info("Health check requested");

        return new HealthResponse(
                "UP",
                "RepoPulse Backend"
        );
    }
}