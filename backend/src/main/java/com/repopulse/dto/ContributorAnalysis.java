package com.repopulse.dto;

public record ContributorAnalysis(
        String name,
        String email,
        int commitCount
) {}