package com.repopulse.service;

import com.repopulse.dto.JavaRepositoryAnalysis;
import com.repopulse.dto.RiskScore;
import com.repopulse.entity.Analysis;
import com.repopulse.entity.AnalysisStatus;
import com.repopulse.entity.GitHubRepository;
import com.repopulse.repository.AnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class AnalysisPersistenceService {

    private final AnalysisRepository analysisRepository;

    public AnalysisPersistenceService(
            AnalysisRepository analysisRepository) {

        this.analysisRepository = analysisRepository;
    }

    @Transactional
    public Analysis createPendingAnalysis(
            GitHubRepository repository) {

        Analysis analysis = new Analysis();

        analysis.setRepository(repository);
        analysis.setStatus(AnalysisStatus.PENDING);
        analysis.setStartedAt(null);
        analysis.setCompletedAt(null);

        return analysisRepository.save(analysis);
    }

    @Transactional
    public void markRunning(Analysis analysis) {

        analysis.setStatus(AnalysisStatus.RUNNING);
        analysis.setStartedAt(Instant.now());

        analysisRepository.save(analysis);
    }

    @Transactional
    public void markCompleted(
            Analysis analysis,
            JavaRepositoryAnalysis result) {

        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setCompletedAt(Instant.now());

        setMetrics(analysis, result);

        analysisRepository.save(analysis);
    }

    @Transactional
    public void markFailed(
            Analysis analysis,
            String errorMessage) {

        analysis.setStatus(AnalysisStatus.FAILED);
        analysis.setCompletedAt(Instant.now());
        analysis.setErrorMessage(errorMessage);

        analysisRepository.save(analysis);
    }

    private void setMetrics(
            Analysis analysis,
            JavaRepositoryAnalysis result) {

        analysis.setTotalFiles(result.totalFiles());
        analysis.setAnalyzedFiles(result.analyzedFiles());
        analysis.setFailedFiles(result.failedFiles());
        analysis.setTotalLinesOfCode(result.totalLinesOfCode());

        analysis.setTotalClasses(result.totalClasses());
        analysis.setTotalMethods(result.totalMethods());

        analysis.setAverageMethodComplexity(
                result.averageMethodComplexity()
        );

        analysis.setMaxMethodComplexity(
                result.maxMethodComplexity()
        );

        analysis.setComplexMethods(
                result.complexMethods()
        );

        analysis.setTotalClassMethods(
                result.totalClassMethods()
        );

        analysis.setTotalClassFields(
                result.totalClassFields()
        );

        analysis.setMaxClassLoc(
                result.maxClassLoc()
        );

        analysis.setTotalDependencies(
                result.totalDependencies()
        );

        analysis.setAverageClassDependencies(
                result.averageClassDependencies()
        );

        analysis.setMaxClassDependencies(
                result.maxClassDependencies()
        );

        analysis.setHighlyCoupledClasses(
                result.highlyCoupledClasses()
        );

        analysis.setTotalSharedFieldPairs(
                result.totalSharedFieldPairs()
        );

        analysis.setAverageClassCohesion(
                result.averageClassCohesion()
        );

        analysis.setLowCohesionClasses(
                result.lowCohesionClasses()
        );

        analysis.setAverageClassLoc(
                result.averageClassLoc()
        );

        analysis.setAverageMethodLoc(
                result.averageMethodLoc()
        );

        analysis.setComplexMethodPercentage(
                result.complexMethodPercentage()
        );

        analysis.setHighlyCoupledClassPercentage(
                result.highlyCoupledClassPercentage()
        );

        analysis.setLowCohesionClassPercentage(
                result.lowCohesionClassPercentage()
        );

        RiskScore riskScore = result.riskScore();

        if (riskScore != null) {

            analysis.setRiskScore(
                    BigDecimal.valueOf(riskScore.score())
            );

            analysis.setRiskLevel(
                    riskScore.level()
            );

            analysis.setRiskExplanation(
                    riskScore.explanation()
            );

            analysis.setComplexityRisk(
                    riskScore.complexityRisk()
            );

            analysis.setCouplingRisk(
                    riskScore.couplingRisk()
            );

            analysis.setCohesionRisk(
                    riskScore.cohesionRisk()
            );

            analysis.setSizeRisk(
                    riskScore.sizeRisk()
            );

            analysis.setCodeSmellRisk(
                    riskScore.codeSmellRisk()
            );

            analysis.setDependencyRisk(
                    riskScore.dependencyRisk()
            );

            analysis.setArchitectureRisk(
                    riskScore.architectureRisk()
            );

            analysis.setChurnRisk(
                    riskScore.churnRisk()
            );

            analysis.setHotspotRisk(
                    riskScore.hotspotRisk()
            );
        }
    }
}