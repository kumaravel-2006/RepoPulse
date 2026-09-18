package com.repopulse.dto;

import com.repopulse.entity.RiskLevel;

public record RiskScore(
        double score,
        RiskLevel level,
        double complexityRisk,
        double couplingRisk,
        double cohesionRisk,
        double sizeRisk
) {}