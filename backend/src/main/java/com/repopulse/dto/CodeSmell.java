package com.repopulse.dto;

public record CodeSmell(
        String smellType,
        String className,
        String methodName,
        int lineNumber,
        int actualValue,
        int threshold,
        String message
) {
}