package com.repopulse.dto;

import java.util.List;

public record JavaClassCoupling(
        String className,
        int dependencyCount,
        List<String> dependencies
) {}