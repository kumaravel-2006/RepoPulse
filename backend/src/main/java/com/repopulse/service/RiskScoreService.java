package com.repopulse.service;

import com.repopulse.dto.JavaRepositoryAnalysis;
import com.repopulse.entity.RiskLevel;
import com.repopulse.dto.RiskScore;
import org.springframework.stereotype.Service;

@Service
public class RiskScoreService {

    public RiskScore calculateRisk(
            JavaRepositoryAnalysis analysis) {

        double complexityRisk =
                normalize(
                        analysis.complexMethodPercentage(),
                        0,
                        20
                );

        double couplingRisk =
                normalize(
                        analysis.highlyCoupledClassPercentage(),
                        0,
                        100
                );

        double cohesionRisk =
                normalize(
                        1.0 - analysis.averageClassCohesion(),
                        0,
                        1
                );

        double sizeRisk =
                normalize(
                        analysis.averageClassLoc(),
                        50,
                        250
                );

        double score =
                complexityRisk * 0.30
                        + couplingRisk * 0.25
                        + cohesionRisk * 0.25
                        + sizeRisk * 0.20;

        score =
                Math.round(score * 100.0)
                        / 100.0;

        RiskLevel level =
                determineRiskLevel(score);

        return new RiskScore(
                score,
                level,
                round(complexityRisk),
                round(couplingRisk),
                round(cohesionRisk),
                round(sizeRisk)
        );
    }

    private double normalize(
            double value,
            double low,
            double high) {

        if (value <= low) {
            return 0.0;
        }

        if (value >= high) {
            return 100.0;
        }

        return ((value - low)
                / (high - low)) * 100.0;
    }

    private RiskLevel determineRiskLevel(
            double score) {

        if (score < 33.33) {
            return RiskLevel.LOW;
        }

        if (score < 66.67) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.HIGH;
    }

    private double round(double value) {

        return Math.round(value * 100.0)
                / 100.0;
    }
}