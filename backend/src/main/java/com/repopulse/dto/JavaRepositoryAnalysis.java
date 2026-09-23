package com.repopulse.dto;

import com.repopulse.analysis.git.FileOwnership;

import java.util.List;

public record JavaRepositoryAnalysis(

        // ============================================================
        // Repository Information
        // ============================================================

        String repositoryName,


        // ============================================================
        // File Metrics
        // ============================================================

        int totalFiles,
        int analyzedFiles,
        int failedFiles,
        int totalLinesOfCode,


        // ============================================================
        // Class / Method Metrics
        // ============================================================

        int totalClasses,
        int totalMethods,

        double averageMethodComplexity,
        int maxMethodComplexity,
        int complexMethods,

        int totalClassMethods,
        int totalClassFields,
        int maxClassLoc,


        // ============================================================
        // Coupling Metrics
        // ============================================================

        int totalDependencies,
        double averageClassDependencies,
        int maxClassDependencies,
        int highlyCoupledClasses,


        // ============================================================
        // Cohesion Metrics
        // ============================================================

        int totalSharedFieldPairs,
        double averageClassCohesion,
        int lowCohesionClasses,


        // ============================================================
        // Repository-Level Metrics
        // ============================================================

        double averageClassLoc,
        double averageMethodLoc,
        double complexMethodPercentage,
        double highlyCoupledClassPercentage,
        double lowCohesionClassPercentage,


        // ============================================================
        // Phase 7 — Code Smells
        // ============================================================

        List<CodeSmell> codeSmells,


        // ============================================================
        // Phase 8 — Dependency Analysis
        // ============================================================

        List<CircularDependency> circularDependencies,
        List<ArchitectureViolation> architectureViolations,


        // ============================================================
        // Phase 9 — Git History
        // ============================================================

        GitCommitAnalysis gitCommitAnalysis,
        List<FileChurn> fileChurn,
        List<ContributorAnalysis> contributors,
        List<HotspotAnalysis> hotspots,
        List<FileOwnership> ownership,


        // ============================================================
        // Risk
        // ============================================================

        RiskScore riskScore

) {
}