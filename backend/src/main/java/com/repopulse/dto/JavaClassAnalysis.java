package com.repopulse.dto;

import java.util.List;

public record JavaClassAnalysis(
        String name,
        String packageName,
        int loc,
        int methodCount,
        int fieldCount,
        int dependencyCount,
        List<String> dependencies,

        int sharedFieldPairs,
        int totalMethodPairs,
        double cohesion
) {}