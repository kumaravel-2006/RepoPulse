package com.repopulse.service;

import com.repopulse.dto.*;
import org.springframework.stereotype.Service;
import com.repopulse.service.CircularDependencyService;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Service
public class JavaRepositoryAnalysisService {

    private final JavaAnalysisService javaAnalysisService;
    private final RiskScoreService riskScoreService;
    private final DependencyGraphService dependencyGraphService;
    private final PackageDependencyGraphService packageDependencyGraphService;
    private final CircularDependencyService circularDependencyService;
    private final ArchitectureViolationService architectureViolationService;
    private final GitHistoryService gitHistoryService;


    public JavaRepositoryAnalysisService(
            JavaAnalysisService javaAnalysisService,
            RiskScoreService riskScoreService,
            DependencyGraphService dependencyGraphService,
            PackageDependencyGraphService packageDependencyGraphService,
            CircularDependencyService circularDependencyService,
            ArchitectureViolationService architectureViolationService,
            GitHistoryService gitHistoryService) {

        this.javaAnalysisService = javaAnalysisService;
        this.riskScoreService = riskScoreService;
        this.dependencyGraphService = dependencyGraphService;
        this.packageDependencyGraphService = packageDependencyGraphService;
        this.circularDependencyService = circularDependencyService;
        this.architectureViolationService = architectureViolationService;
        this.gitHistoryService = gitHistoryService;
    }

    public JavaRepositoryAnalysis analyzeRepository(
            Path repositoryPath,
            String repositoryName) throws Exception {

        // --------------------------------------------------
        // FIND JAVA FILES
        // --------------------------------------------------

        List<Path> javaFiles =
                javaAnalysisService
                        .findJavaFiles(repositoryPath);

        // --------------------------------------------------
        // BUILD GLOBAL REPOSITORY CLASS SET
        // --------------------------------------------------

        Set<String> repositoryClasses =
                javaAnalysisService
                        .findRepositoryClasses(javaFiles);

        // --------------------------------------------------
        // ANALYZE ALL JAVA FILES
        // --------------------------------------------------

        List<JavaFileAnalysis> fileAnalyses =
                javaFiles.stream()
                        .map(file -> {

                            try {

                                return javaAnalysisService
                                        .analyzeJavaFile(
                                                file,
                                                repositoryClasses
                                        );

                            } catch (Exception e) {

                                return new JavaFileAnalysis(
                                        file.toString(),
                                        0,
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        e.getMessage()
                                );
                            }

                        })
                        .toList();

        // --------------------------------------------------
        // CODE SMELLS
        // --------------------------------------------------

        List<CodeSmell> allCodeSmells =
                fileAnalyses.stream()
                        .flatMap(file ->
                                file.codeSmells().stream())
                        .toList();

        // --------------------------------------------------
        // FILE METRICS
        // --------------------------------------------------

        int totalFiles =
                fileAnalyses.size();

        int failedFiles =
                (int) fileAnalyses.stream()
                        .filter(file ->
                                file.error() != null)
                        .count();

        int analyzedFiles =
                totalFiles - failedFiles;

        int totalLinesOfCode =
                fileAnalyses.stream()
                        .mapToInt(
                                JavaFileAnalysis::loc)
                        .sum();

        // --------------------------------------------------
        // CLASS / METHOD METRICS
        // --------------------------------------------------

        int totalClasses =
                fileAnalyses.stream()
                        .mapToInt(file ->
                                file.classes().size())
                        .sum();

        int totalMethods =
                fileAnalyses.stream()
                        .mapToInt(file ->
                                file.methods().size())
                        .sum();

        int totalClassMethods =
                fileAnalyses.stream()
                        .flatMap(file ->
                                file.classMetrics()
                                        .stream())
                        .mapToInt(
                                JavaClassAnalysis::methodCount)
                        .sum();

        int totalClassFields =
                fileAnalyses.stream()
                        .flatMap(file ->
                                file.classMetrics()
                                        .stream())
                        .mapToInt(
                                JavaClassAnalysis::fieldCount)
                        .sum();

        int maxClassLoc =
                fileAnalyses.stream()
                        .flatMap(file ->
                                file.classMetrics()
                                        .stream())
                        .mapToInt(
                                JavaClassAnalysis::loc)
                        .max()
                        .orElse(0);

        // --------------------------------------------------
        // COMPLEXITY METRICS
        // --------------------------------------------------

        int maxMethodComplexity =
                fileAnalyses.stream()
                        .flatMap(file ->
                                file.methods()
                                        .stream())
                        .mapToInt(
                                JavaMethodAnalysis::complexity)
                        .max()
                        .orElse(0);

        long totalComplexity =
                fileAnalyses.stream()
                        .flatMap(file ->
                                file.methods()
                                        .stream())
                        .mapToLong(
                                JavaMethodAnalysis::complexity)
                        .sum();

        double averageMethodComplexity =
                totalMethods == 0
                        ? 0.0
                        : (double) totalComplexity
                        / totalMethods;

        int complexMethods =
                (int) fileAnalyses.stream()
                        .flatMap(file ->
                                file.methods()
                                        .stream())
                        .filter(method ->
                                method.complexity() >= 6)
                        .count();

        // --------------------------------------------------
        // ALL CLASS ANALYSES
        // --------------------------------------------------

        List<JavaClassAnalysis> allClasses =
                fileAnalyses.stream()
                        .flatMap(file ->
                                file.classMetrics()
                                        .stream())
                        .toList();

        DependencyGraph dependencyGraph =
                dependencyGraphService.buildClassDependencyGraph(allClasses);

        PackageDependencyGraph packageDependencyGraph =
                packageDependencyGraphService
                        .buildPackageDependencyGraph(allClasses);

        System.out.println("========== PACKAGE DEPENDENCY GRAPH ==========");
        System.out.println(packageDependencyGraph);
        System.out.println("========== END PACKAGE DEPENDENCY GRAPH ==========");

        List<CircularDependency> circularDependencies =
                circularDependencyService.detectCycles(dependencyGraph);

        System.out.println("========== CIRCULAR DEPENDENCIES ==========");
        System.out.println(circularDependencies);
        System.out.println("========== END CIRCULAR DEPENDENCIES ==========");

        List<ArchitectureViolation> architectureViolations =
                architectureViolationService
                        .detectViolations(packageDependencyGraph);

        System.out.println("========== ARCHITECTURE VIOLATIONS ==========");
        System.out.println(architectureViolations);
        System.out.println("========== END ARCHITECTURE VIOLATIONS ==========");

        GitCommitAnalysis gitCommitAnalysis =
                gitHistoryService.analyzeCommitHistory(repositoryPath);

        System.out.println("========== GIT COMMIT ANALYSIS ==========");
        System.out.println(gitCommitAnalysis);
        System.out.println("========== END GIT COMMIT ANALYSIS ==========");

        List<FileChurn> fileChurn =
                gitHistoryService.analyzeFileChurn(repositoryPath);

        System.out.println("========== FILE CHURN ==========");
        System.out.println(fileChurn);
        System.out.println("========== END FILE CHURN ==========");

        List<ContributorAnalysis> contributors =
                gitHistoryService.analyzeContributors(repositoryPath);

        System.out.println("========== CONTRIBUTORS ==========");
        System.out.println(contributors);
        System.out.println("========== END CONTRIBUTORS ==========");

        List<HotspotAnalysis> hotspots =
                gitHistoryService.analyzeHotspots(
                        fileChurn,
                        fileAnalyses
                );

        System.out.println("========== HOTSPOTS ==========");
        System.out.println(hotspots);
        System.out.println("========== END HOTSPOTS ==========");

        // --------------------------------------------------
        // COUPLING METRICS
        // --------------------------------------------------

        int totalDependencies =
                allClasses.stream()
                        .mapToInt(
                                JavaClassAnalysis::dependencyCount)
                        .sum();

        double averageClassDependencies =
                allClasses.isEmpty()
                        ? 0.0
                        : (double) totalDependencies
                        / allClasses.size();

        int maxClassDependencies =
                allClasses.stream()
                        .mapToInt(
                                JavaClassAnalysis::dependencyCount)
                        .max()
                        .orElse(0);

        int highlyCoupledClasses =
                (int) allClasses.stream()
                        .filter(classAnalysis ->
                                classAnalysis
                                        .dependencyCount()
                                        >= 5)
                        .count();

        // --------------------------------------------------
        // COHESION METRICS
        // --------------------------------------------------

        int totalSharedFieldPairs =
                allClasses.stream()
                        .mapToInt(
                                JavaClassAnalysis::sharedFieldPairs)
                        .sum();

        double averageClassCohesion =
                allClasses.isEmpty()
                        ? 0.0
                        : allClasses.stream()
                        .mapToDouble(
                                JavaClassAnalysis::cohesion)
                        .average()
                        .orElse(0.0);

        int lowCohesionClasses =
                (int) allClasses.stream()
                        .filter(classAnalysis ->
                                classAnalysis
                                        .cohesion()
                                        < 0.5)
                        .count();

        // --------------------------------------------------
        // REPOSITORY METRICS
        // --------------------------------------------------

        int totalClassLoc =
                allClasses.stream()
                        .mapToInt(
                                JavaClassAnalysis::loc)
                        .sum();

        double averageClassLoc =
                totalClasses == 0
                        ? 0.0
                        : (double) totalClassLoc
                        / totalClasses;

        int totalMethodLoc =
                fileAnalyses.stream()
                        .flatMap(file ->
                                file.methods()
                                        .stream())
                        .mapToInt(
                                JavaMethodAnalysis::loc)
                        .sum();

        double averageMethodLoc =
                totalMethods == 0
                        ? 0.0
                        : (double) totalMethodLoc
                        / totalMethods;

        double complexMethodPercentage =
                totalMethods == 0
                        ? 0.0
                        : ((double) complexMethods
                        / totalMethods) * 100;

        double highlyCoupledClassPercentage =
                totalClasses == 0
                        ? 0.0
                        : ((double) highlyCoupledClasses
                        / totalClasses) * 100;

        double lowCohesionClassPercentage =
                totalClasses == 0
                        ? 0.0
                        : ((double) lowCohesionClasses
                        / totalClasses) * 100;

        // --------------------------------------------------
        // CREATE ANALYSIS WITHOUT RISK
        // --------------------------------------------------

        JavaRepositoryAnalysis analysis =
                new JavaRepositoryAnalysis(

                        repositoryName,

                        totalFiles,
                        analyzedFiles,
                        failedFiles,
                        totalLinesOfCode,

                        totalClasses,
                        totalMethods,

                        Math.round(
                                averageMethodComplexity
                                        * 100.0)
                                / 100.0,

                        maxMethodComplexity,
                        complexMethods,

                        totalClassMethods,
                        totalClassFields,
                        maxClassLoc,

                        totalDependencies,

                        Math.round(
                                averageClassDependencies
                                        * 100.0)
                                / 100.0,

                        maxClassDependencies,
                        highlyCoupledClasses,

                        totalSharedFieldPairs,

                        Math.round(
                                averageClassCohesion
                                        * 100.0)
                                / 100.0,

                        lowCohesionClasses,

                        Math.round(
                                averageClassLoc
                                        * 100.0)
                                / 100.0,

                        Math.round(
                                averageMethodLoc
                                        * 100.0)
                                / 100.0,

                        Math.round(
                                complexMethodPercentage
                                        * 100.0)
                                / 100.0,

                        Math.round(
                                highlyCoupledClassPercentage
                                        * 100.0)
                                / 100.0,

                        Math.round(
                                lowCohesionClassPercentage
                                        * 100.0)
                                / 100.0,

                        allCodeSmells,

                        null
                );

        // --------------------------------------------------
        // CALCULATE RISK
        // --------------------------------------------------

        RiskScore riskScore =
                riskScoreService
                        .calculateRisk(analysis);

        // --------------------------------------------------
        // RETURN FINAL ANALYSIS
        // --------------------------------------------------

        return new JavaRepositoryAnalysis(

                analysis.repositoryName(),

                analysis.totalFiles(),
                analysis.analyzedFiles(),
                analysis.failedFiles(),
                analysis.totalLinesOfCode(),

                analysis.totalClasses(),
                analysis.totalMethods(),

                analysis.averageMethodComplexity(),
                analysis.maxMethodComplexity(),
                analysis.complexMethods(),

                analysis.totalClassMethods(),
                analysis.totalClassFields(),
                analysis.maxClassLoc(),

                analysis.totalDependencies(),
                analysis.averageClassDependencies(),
                analysis.maxClassDependencies(),
                analysis.highlyCoupledClasses(),

                analysis.totalSharedFieldPairs(),
                analysis.averageClassCohesion(),
                analysis.lowCohesionClasses(),

                analysis.averageClassLoc(),
                analysis.averageMethodLoc(),
                analysis.complexMethodPercentage(),
                analysis.highlyCoupledClassPercentage(),
                analysis.lowCohesionClassPercentage(),

                analysis.codeSmells(),

                riskScore
        );
    }
}