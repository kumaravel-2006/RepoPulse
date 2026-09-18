package com.repopulse.dto;

import java.util.List;

public record JavaMethodAnalysis(
        String className,
        String name,
        String returnType,
        List<String> parameters,
        int parameterCount,
        int loc,
        int complexity,
        int maxNestingDepth,
        int lineNumber,
        String normalizedBody
) {
}