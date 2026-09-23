package com.repopulse.service;

import com.repopulse.dto.JavaRepositoryAnalysis;
import com.repopulse.dto.RiskScore;
import com.repopulse.entity.RiskLevel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RiskScoreService {

    public RiskScore calculateRisk(
            JavaRepositoryAnalysis analysis) {

        // ============================================
        // 10.2 — METRIC NORMALIZATION
        // ============================================

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


        // ============================================
        // 10.3 — CODE SMELL RISK
        // ============================================

        double codeSmellDensity = 0.0;

        if (analysis.totalLinesOfCode() > 0) {

            codeSmellDensity =
                    analysis.codeSmells().size()
                            * 1000.0
                            / analysis.totalLinesOfCode();
        }

        double codeSmellRisk =
                normalize(
                        codeSmellDensity,
                        0,
                        10
                );


        // ============================================
        // 10.3 — DEPENDENCY RISK
        // ============================================

        double dependencyRisk =
                calculateDependencyRisk(analysis);


        // ============================================
        // 10.3 — ARCHITECTURE RISK
        // ============================================

        double architectureRisk =
                calculateArchitectureRisk(analysis);


        // ============================================
        // 10.3 — GIT CHURN RISK
        // ============================================

        double churnRisk =
                calculateChurnRisk(analysis);


        // ============================================
        // 10.3 — HOTSPOT RISK
        // ============================================

        double hotspotRisk =
                calculateHotspotRisk(analysis);


        // ============================================
        // 10.4 — WEIGHTED RISK SCORE
        // ============================================

        double complexityContribution =
                complexityRisk * 0.20;

        double couplingContribution =
                couplingRisk * 0.15;

        double cohesionContribution =
                cohesionRisk * 0.15;

        double sizeContribution =
                sizeRisk * 0.10;

        double codeSmellContribution =
                codeSmellRisk * 0.15;

        double dependencyContribution =
                dependencyRisk * 0.10;

        double churnContribution =
                churnRisk * 0.10;

        double hotspotContribution =
                hotspotRisk * 0.05;

        double score =
                complexityContribution
                        + couplingContribution
                        + cohesionContribution
                        + sizeContribution
                        + codeSmellContribution
                        + dependencyContribution
                        + churnContribution
                        + hotspotContribution;

        score = round(score);


        // ============================================
        // 10.5 — RISK LEVEL
        // ============================================

        RiskLevel level =
                determineRiskLevel(score);


        // ============================================
        // 10.6 — RISK EXPLANATION
        // ============================================

        String explanation =
                generateExplanation(
                        score,
                        level,
                        complexityContribution,
                        couplingContribution,
                        cohesionContribution,
                        sizeContribution,
                        codeSmellContribution,
                        dependencyContribution,
                        churnContribution,
                        hotspotContribution
                );


        // ============================================
        // FINAL RISK SCORE
        // ============================================

        return new RiskScore(
                score,
                level,

                round(complexityRisk),
                round(couplingRisk),
                round(cohesionRisk),
                round(sizeRisk),

                round(codeSmellRisk),
                round(dependencyRisk),
                round(architectureRisk),
                round(churnRisk),
                round(hotspotRisk),

                explanation
        );
    }


    // =================================================
    // DEPENDENCY RISK
    // =================================================

    private double calculateDependencyRisk(
            JavaRepositoryAnalysis analysis) {

        return normalize(
                analysis.averageClassDependencies(),
                0,
                5
        );
    }


    // =================================================
    // ARCHITECTURE RISK
    // =================================================

    private double calculateArchitectureRisk(
            JavaRepositoryAnalysis analysis) {

        double violationPercentage = 0.0;

        if (analysis.totalClasses() > 0) {

            violationPercentage =
                    analysis.architectureViolations().size()
                            * 100.0
                            / analysis.totalClasses();
        }

        return normalize(
                violationPercentage,
                0,
                20
        );
    }


    // =================================================
    // GIT CHURN RISK
    // =================================================

    private double calculateChurnRisk(
            JavaRepositoryAnalysis analysis) {

        if (analysis.fileChurn() == null
                || analysis.fileChurn().isEmpty()) {

            return 0.0;
        }

        double totalChanges =
                analysis.fileChurn()
                        .stream()
                        .mapToInt(file -> file.changeCount())
                        .sum();

        double averageChanges =
                totalChanges
                        / analysis.fileChurn().size();

        return normalize(
                averageChanges,
                0,
                10
        );
    }


    // =================================================
    // HOTSPOT RISK
    // =================================================

    private double calculateHotspotRisk(
            JavaRepositoryAnalysis analysis) {

        if (analysis.hotspots() == null
                || analysis.hotspots().isEmpty()) {

            return 0.0;
        }

        double totalHotspotScore = 0.0;

        for (var hotspot : analysis.hotspots()) {

            double hotspotScore =
                    hotspot.changeCount()
                            + hotspot.codeSmellCount()
                            + hotspot.dependencyCount()
                            + hotspot.complexity();

            totalHotspotScore += hotspotScore;
        }

        double averageHotspotScore =
                totalHotspotScore
                        / analysis.hotspots().size();

        return normalize(
                averageHotspotScore,
                0,
                20
        );
    }


    // =================================================
    // 10.6 — EXPLANATION
    // =================================================

    private String generateExplanation(
            double score,
            RiskLevel level,
            double complexityContribution,
            double couplingContribution,
            double cohesionContribution,
            double sizeContribution,
            double codeSmellContribution,
            double dependencyContribution,
            double churnContribution,
            double hotspotContribution) {

        List<RiskContribution> contributions =
                new ArrayList<>();

        contributions.add(
                new RiskContribution(
                        "complexity",
                        complexityContribution
                )
        );

        contributions.add(
                new RiskContribution(
                        "coupling",
                        couplingContribution
                )
        );

        contributions.add(
                new RiskContribution(
                        "cohesion",
                        cohesionContribution
                )
        );

        contributions.add(
                new RiskContribution(
                        "size",
                        sizeContribution
                )
        );

        contributions.add(
                new RiskContribution(
                        "code smells",
                        codeSmellContribution
                )
        );

        contributions.add(
                new RiskContribution(
                        "dependencies",
                        dependencyContribution
                )
        );

        contributions.add(
                new RiskContribution(
                        "Git churn",
                        churnContribution
                )
        );

        contributions.add(
                new RiskContribution(
                        "hotspots",
                        hotspotContribution
                )
        );


        contributions.sort(
                Comparator.comparingDouble(
                        RiskContribution::value
                ).reversed()
        );


        StringBuilder explanation =
                new StringBuilder();

        explanation.append(
                "Overall risk is "
        );

        explanation.append(level);

        explanation.append(
                " with a score of "
        );

        explanation.append(
                String.format("%.2f", score)
        );

        explanation.append(". ");


        List<RiskContribution> significant =
                contributions.stream()
                        .filter(
                                contribution ->
                                        contribution.value() > 0
                        )
                        .limit(3)
                        .toList();


        if (significant.isEmpty()) {

            explanation.append(
                    "No significant risk contributors were detected."
            );

            return explanation.toString();
        }


        explanation.append(
                "The main contributors are "
        );


        for (int i = 0;
             i < significant.size();
             i++) {

            RiskContribution contribution =
                    significant.get(i);

            explanation.append(
                    contribution.name()
            );

            explanation.append(
                    " ("
            );

            explanation.append(
                    String.format(
                            "%.2f",
                            contribution.value()
                    )
            );

            explanation.append(
                    " points)"
            );


            if (i < significant.size() - 2) {

                explanation.append(", ");

            } else if (
                    i == significant.size() - 2) {

                explanation.append(" and ");
            }
        }

        explanation.append(".");

        return explanation.toString();
    }


    // =================================================
    // RISK CONTRIBUTION
    // =================================================

    private record RiskContribution(
            String name,
            double value
    ) {
    }


    // =================================================
    // NORMALIZATION
    // =================================================

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


    // =================================================
    // RISK LEVEL
    // =================================================

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


    // =================================================
    // ROUNDING
    // =================================================

    private double round(double value) {

        return Math.round(value * 100.0)
                / 100.0;
    }
}