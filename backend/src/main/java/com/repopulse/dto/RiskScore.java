package com.repopulse.dto;

import com.repopulse.entity.RiskLevel;

public record RiskScore(

        double score,

        RiskLevel level,

        double complexityRisk,
        double couplingRisk,
        double cohesionRisk,
        double sizeRisk,

        double codeSmellRisk,
        double dependencyRisk,
        double architectureRisk,
        double churnRisk,
        double hotspotRisk,

        String explanation

) {}