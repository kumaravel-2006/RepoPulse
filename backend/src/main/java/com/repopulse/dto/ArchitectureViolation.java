package com.repopulse.dto;

public record ArchitectureViolation(
        String sourcePackage,
        String targetPackage,
        String message
) {}