package com.repopulse.dto;

import java.util.List;
import com.repopulse.dto.CodeSmell;

public record JavaRepositoryAnalysis(

        String repositoryName,

        int totalFiles,
        int analyzedFiles,
        int failedFiles,
        int totalLinesOfCode,

        int totalClasses,
        int totalMethods,

        double averageMethodComplexity,
        int maxMethodComplexity,
        int complexMethods,

        int totalClassMethods,
        int totalClassFields,
        int maxClassLoc,

        int totalDependencies,
        double averageClassDependencies,
        int maxClassDependencies,
        int highlyCoupledClasses,

        int totalSharedFieldPairs,
        double averageClassCohesion,
        int lowCohesionClasses,

        double averageClassLoc,
        double averageMethodLoc,
        double complexMethodPercentage,
        double highlyCoupledClassPercentage,
        double lowCohesionClassPercentage,
        List<CodeSmell> codeSmells,
        RiskScore riskScore

) {}