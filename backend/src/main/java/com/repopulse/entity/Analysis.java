package com.repopulse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private GitHubRepository repository;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "risk_explanation")
    private String riskExplanation;

    @Column(name = "complexity_risk")
    private double complexityRisk;

    @Column(name = "coupling_risk")
    private double couplingRisk;

    @Column(name = "cohesion_risk")
    private double cohesionRisk;

    @Column(name = "size_risk")
    private double sizeRisk;

    @Column(name = "code_smell_risk")
    private double codeSmellRisk;

    @Column(name = "dependency_risk")
    private double dependencyRisk;

    @Column(name = "architecture_risk")
    private double architectureRisk;

    @Column(name = "churn_risk")
    private double churnRisk;

    @Column(name = "hotspot_risk")
    private double hotspotRisk;

    @Column(name = "total_files", nullable = false)
    private int totalFiles;

    @Column(name = "analyzed_files", nullable = false)
    private int analyzedFiles;

    @Column(name = "failed_files", nullable = false)
    private int failedFiles;

    @Column(name = "total_lines_of_code", nullable = false)
    private int totalLinesOfCode;

    @Column(name = "total_classes", nullable = false)
    private int totalClasses;

    @Column(name = "total_methods", nullable = false)
    private int totalMethods;

    @Column(name = "average_method_complexity")
    private double averageMethodComplexity;

    @Column(name = "max_method_complexity")
    private int maxMethodComplexity;

    @Column(name = "complex_methods")
    private int complexMethods;

    @Column(name = "total_class_methods")
    private int totalClassMethods;

    @Column(name = "total_class_fields")
    private int totalClassFields;

    @Column(name = "max_class_loc")
    private int maxClassLoc;

    @Column(name = "total_dependencies")
    private int totalDependencies;

    @Column(name = "average_class_dependencies")
    private double averageClassDependencies;

    @Column(name = "max_class_dependencies")
    private int maxClassDependencies;

    @Column(name = "highly_coupled_classes")
    private int highlyCoupledClasses;

    @Column(name = "total_shared_field_pairs")
    private int totalSharedFieldPairs;

    @Column(name = "average_class_cohesion")
    private double averageClassCohesion;

    @Column(name = "low_cohesion_classes")
    private int lowCohesionClasses;

    @Column(name = "average_class_loc")
    private double averageClassLoc;

    @Column(name = "average_method_loc")
    private double averageMethodLoc;

    @Column(name = "complex_method_percentage")
    private double complexMethodPercentage;

    @Column(name = "highly_coupled_class_percentage")
    private double highlyCoupledClassPercentage;

    @Column(name = "low_cohesion_class_percentage")
    private double lowCohesionClassPercentage;

    @Column(name = "error_message")
    private String errorMessage;

    public Analysis() {
    }

    public Long getId() {
        return id;
    }

    public GitHubRepository getRepository() {
        return repository;
    }

    public void setRepository(GitHubRepository repository) {
        this.repository = repository;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskExplanation() {
        return riskExplanation;
    }

    public void setRiskExplanation(String riskExplanation) {
        this.riskExplanation = riskExplanation;
    }

    public double getComplexityRisk() {
        return complexityRisk;
    }

    public void setComplexityRisk(double complexityRisk) {
        this.complexityRisk = complexityRisk;
    }

    public double getCouplingRisk() {
        return couplingRisk;
    }

    public void setCouplingRisk(double couplingRisk) {
        this.couplingRisk = couplingRisk;
    }

    public double getCohesionRisk() {
        return cohesionRisk;
    }

    public void setCohesionRisk(double cohesionRisk) {
        this.cohesionRisk = cohesionRisk;
    }

    public double getSizeRisk() {
        return sizeRisk;
    }

    public void setSizeRisk(double sizeRisk) {
        this.sizeRisk = sizeRisk;
    }

    public double getCodeSmellRisk() {
        return codeSmellRisk;
    }

    public void setCodeSmellRisk(double codeSmellRisk) {
        this.codeSmellRisk = codeSmellRisk;
    }

    public double getDependencyRisk() {
        return dependencyRisk;
    }

    public void setDependencyRisk(double dependencyRisk) {
        this.dependencyRisk = dependencyRisk;
    }

    public double getArchitectureRisk() {
        return architectureRisk;
    }

    public void setArchitectureRisk(double architectureRisk) {
        this.architectureRisk = architectureRisk;
    }

    public double getChurnRisk() {
        return churnRisk;
    }

    public void setChurnRisk(double churnRisk) {
        this.churnRisk = churnRisk;
    }

    public double getHotspotRisk() {
        return hotspotRisk;
    }

    public void setHotspotRisk(double hotspotRisk) {
        this.hotspotRisk = hotspotRisk;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getAnalyzedFiles() {
        return analyzedFiles;
    }

    public void setAnalyzedFiles(int analyzedFiles) {
        this.analyzedFiles = analyzedFiles;
    }

    public int getFailedFiles() {
        return failedFiles;
    }

    public void setFailedFiles(int failedFiles) {
        this.failedFiles = failedFiles;
    }

    public int getTotalLinesOfCode() {
        return totalLinesOfCode;
    }

    public void setTotalLinesOfCode(int totalLinesOfCode) {
        this.totalLinesOfCode = totalLinesOfCode;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getTotalMethods() {
        return totalMethods;
    }

    public void setTotalMethods(int totalMethods) {
        this.totalMethods = totalMethods;
    }

    public double getAverageMethodComplexity() {
        return averageMethodComplexity;
    }

    public void setAverageMethodComplexity(double averageMethodComplexity) {
        this.averageMethodComplexity = averageMethodComplexity;
    }

    public int getMaxMethodComplexity() {
        return maxMethodComplexity;
    }

    public void setMaxMethodComplexity(int maxMethodComplexity) {
        this.maxMethodComplexity = maxMethodComplexity;
    }

    public int getComplexMethods() {
        return complexMethods;
    }

    public void setComplexMethods(int complexMethods) {
        this.complexMethods = complexMethods;
    }

    public int getTotalClassMethods() {
        return totalClassMethods;
    }

    public void setTotalClassMethods(int totalClassMethods) {
        this.totalClassMethods = totalClassMethods;
    }

    public int getTotalClassFields() {
        return totalClassFields;
    }

    public void setTotalClassFields(int totalClassFields) {
        this.totalClassFields = totalClassFields;
    }

    public int getMaxClassLoc() {
        return maxClassLoc;
    }

    public void setMaxClassLoc(int maxClassLoc) {
        this.maxClassLoc = maxClassLoc;
    }

    public int getTotalDependencies() {
        return totalDependencies;
    }

    public void setTotalDependencies(int totalDependencies) {
        this.totalDependencies = totalDependencies;
    }

    public double getAverageClassDependencies() {
        return averageClassDependencies;
    }

    public void setAverageClassDependencies(double averageClassDependencies) {
        this.averageClassDependencies = averageClassDependencies;
    }

    public int getMaxClassDependencies() {
        return maxClassDependencies;
    }

    public void setMaxClassDependencies(int maxClassDependencies) {
        this.maxClassDependencies = maxClassDependencies;
    }

    public int getHighlyCoupledClasses() {
        return highlyCoupledClasses;
    }

    public void setHighlyCoupledClasses(int highlyCoupledClasses) {
        this.highlyCoupledClasses = highlyCoupledClasses;
    }

    public int getTotalSharedFieldPairs() {
        return totalSharedFieldPairs;
    }

    public void setTotalSharedFieldPairs(int totalSharedFieldPairs) {
        this.totalSharedFieldPairs = totalSharedFieldPairs;
    }

    public double getAverageClassCohesion() {
        return averageClassCohesion;
    }

    public void setAverageClassCohesion(double averageClassCohesion) {
        this.averageClassCohesion = averageClassCohesion;
    }

    public int getLowCohesionClasses() {
        return lowCohesionClasses;
    }

    public void setLowCohesionClasses(int lowCohesionClasses) {
        this.lowCohesionClasses = lowCohesionClasses;
    }

    public double getAverageClassLoc() {
        return averageClassLoc;
    }

    public void setAverageClassLoc(double averageClassLoc) {
        this.averageClassLoc = averageClassLoc;
    }

    public double getAverageMethodLoc() {
        return averageMethodLoc;
    }

    public void setAverageMethodLoc(double averageMethodLoc) {
        this.averageMethodLoc = averageMethodLoc;
    }

    public double getComplexMethodPercentage() {
        return complexMethodPercentage;
    }

    public void setComplexMethodPercentage(double complexMethodPercentage) {
        this.complexMethodPercentage = complexMethodPercentage;
    }

    public double getHighlyCoupledClassPercentage() {
        return highlyCoupledClassPercentage;
    }

    public void setHighlyCoupledClassPercentage(
            double highlyCoupledClassPercentage) {
        this.highlyCoupledClassPercentage =
                highlyCoupledClassPercentage;
    }

    public double getLowCohesionClassPercentage() {
        return lowCohesionClassPercentage;
    }

    public void setLowCohesionClassPercentage(
            double lowCohesionClassPercentage) {
        this.lowCohesionClassPercentage =
                lowCohesionClassPercentage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}