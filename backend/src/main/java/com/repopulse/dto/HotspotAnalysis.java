package com.repopulse.dto;

public record HotspotAnalysis(
        String file,
        int changeCount,
        int loc,
        int complexity,
        int codeSmellCount,
        int dependencyCount
) {}