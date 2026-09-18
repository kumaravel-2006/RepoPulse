package com.repopulse.service;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.repopulse.dto.*;
import org.springframework.stereotype.Service;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.repopulse.dto.JavaMethodAnalysis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.repopulse.dto.CodeSmell;


@Service
public class JavaAnalysisService {

    private final ComplexityMetricService complexityMetricService;
    private final CodeSmellService codeSmellService;

    public JavaAnalysisService(
            ComplexityMetricService complexityMetricService,
            CodeSmellService codeSmellService) {

        this.complexityMetricService = complexityMetricService;
        this.codeSmellService = codeSmellService;
    }

    // --------------------------------------------------
    // PARSE JAVA FILE
    // --------------------------------------------------

    public CompilationUnit parseJavaFile(
            Path filePath) throws Exception {

        StaticJavaParser
                .getParserConfiguration()
                .setLanguageLevel(
                        ParserConfiguration.LanguageLevel.JAVA_21);

        return StaticJavaParser.parse(filePath);
    }

    // --------------------------------------------------
    // EXTRACT CLASSES
    // --------------------------------------------------

    public List<String> extractClasses(
            CompilationUnit compilationUnit) {

        return compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .toList();
    }

    // --------------------------------------------------
    // FIND ALL REPOSITORY CLASSES
    // --------------------------------------------------

    public Set<String> findRepositoryClasses(
            List<Path> javaFiles) {

        Set<String> repositoryClasses =
                new LinkedHashSet<>();

        for (Path file : javaFiles) {

            try {

                CompilationUnit compilationUnit =
                        parseJavaFile(file);

                compilationUnit
                        .findAll(
                                ClassOrInterfaceDeclaration.class)
                        .forEach(classDeclaration ->
                                repositoryClasses.add(
                                        classDeclaration
                                                .getNameAsString()
                                )
                        );

            } catch (Exception ignored) {
                // Parsing failures are handled later
                // during individual file analysis.
            }
        }

        return repositoryClasses;
    }

    // --------------------------------------------------
    // EXTRACT METHODS
    // --------------------------------------------------

    public List<JavaMethodAnalysis> extractMethods(
            CompilationUnit compilationUnit) {

        return compilationUnit
                .findAll(MethodDeclaration.class)
                .stream()
                .map(method -> {

                    String className =
                            method.findAncestor(
                                            ClassOrInterfaceDeclaration.class)
                                    .map(ClassOrInterfaceDeclaration::getNameAsString)
                                    .orElse("Unknown");

                    String name =
                            method.getNameAsString();

                    String returnType =
                            method.getTypeAsString();

                    List<String> parameters =
                            method.getParameters()
                                    .stream()
                                    .map(parameter ->
                                            parameter.toString())
                                    .toList();

                    int parameterCount =
                            method.getParameters().size();

                    int loc =
                            method.getRange()
                                    .map(range ->
                                            range.end.line
                                                    - range.begin.line
                                                    + 1)
                                    .orElse(0);

                    int complexity =
                            complexityMetricService
                                    .calculate(method);

                    int maxNestingDepth =
                            calculateMaxNestingDepth(method);

                    int lineNumber =
                            method.getRange()
                                    .map(range ->
                                            range.begin.line)
                                    .orElse(0);

                    String normalizedBody = method.getBody()
                            .map(body -> body.toString().replaceAll("\\s+", ""))
                            .orElse("");

                    return new JavaMethodAnalysis(
                            className,
                            name,
                            returnType,
                            parameters,
                            parameterCount,
                            loc,
                            complexity,
                            maxNestingDepth,
                            lineNumber,
                            normalizedBody
                    );
                })
                .toList();
    }

    // --------------------------------------------------
    // CALCULATE FILE LOC
    // --------------------------------------------------

    public int calculateLoc(
            Path filePath) throws Exception {

        try (var lines =
                     Files.lines(filePath)) {

            return (int) lines.count();
        }
    }

    // --------------------------------------------------
    // ANALYZE JAVA FILE
    // --------------------------------------------------

    public JavaFileAnalysis analyzeJavaFile(
            Path filePath,
            Set<String> repositoryClasses) throws Exception {

        CompilationUnit compilationUnit =
                parseJavaFile(filePath);

        List<String> classes =
                extractClasses(compilationUnit);

        List<JavaMethodAnalysis> methods =
                extractMethods(compilationUnit);

        List<JavaClassAnalysis> classMetrics =
                extractClassMetrics(
                        compilationUnit,
                        repositoryClasses
                );

        List<CodeSmell> codeSmells = new ArrayList<>();

        codeSmells.addAll(codeSmellService.detectLongMethods(methods));
        codeSmells.addAll(codeSmellService.detectLargeClasses(classMetrics));
        codeSmells.addAll(codeSmellService.detectDeepNesting(methods));
        codeSmells.addAll(
                codeSmellService.detectTooManyParameters(methods)
        );
        codeSmells.addAll(
                codeSmellService.detectEmptyCatchBlocks(compilationUnit)
        );
        codeSmells.addAll(
                codeSmellService.detectDuplicateCode(methods)
        );

        int loc =
                calculateLoc(filePath);

        return new JavaFileAnalysis(
                filePath.toString(),
                loc,
                classes,
                methods,
                classMetrics,
                codeSmells,
                null
        );
    }

    // --------------------------------------------------
    // FIND JAVA FILES
    // --------------------------------------------------

    public List<Path> findJavaFiles(
            Path repositoryPath) throws Exception {

        try (var paths =
                     Files.walk(repositoryPath)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString()
                                    .endsWith(".java"))
                    .toList();
        }
    }

    // --------------------------------------------------
    // MAX NESTING DEPTH
    // --------------------------------------------------

    private int calculateMaxNestingDepth(
            MethodDeclaration method) {

        return calculateNestingDepth(
                method,
                0);
    }

    private int calculateNestingDepth(
            Node node,
            int currentDepth) {

        int nextDepth =
                currentDepth;

        if (isNestingNode(node)) {
            nextDepth++;
        }

        int maxDepth =
                nextDepth;

        for (Node child :
                node.getChildNodes()) {

            maxDepth =
                    Math.max(
                            maxDepth,
                            calculateNestingDepth(
                                    child,
                                    nextDepth)
                    );
        }

        return maxDepth;
    }

    private boolean isNestingNode(
            Node node) {

        return node instanceof IfStmt
                || node instanceof ForStmt
                || node instanceof ForEachStmt
                || node instanceof WhileStmt
                || node instanceof DoStmt
                || node instanceof SwitchStmt
                || node instanceof TryStmt;
    }

    // --------------------------------------------------
    // CLASS METRICS
    // --------------------------------------------------

    public List<JavaClassAnalysis> extractClassMetrics(
            CompilationUnit compilationUnit,
            Set<String> repositoryClasses) {

        return compilationUnit
                .findAll(
                        ClassOrInterfaceDeclaration.class)
                .stream()
                .map(classDeclaration -> {

                    String name =
                            classDeclaration
                                    .getNameAsString();

                    String packageName = compilationUnit
                            .getPackageDeclaration()
                            .map(packageDeclaration -> packageDeclaration.getNameAsString())
                            .orElse("(default)");

                    int loc =
                            classDeclaration
                                    .getRange()
                                    .map(range ->
                                            range.end.line
                                                    - range.begin.line
                                                    + 1)
                                    .orElse(0);

                    int methodCount =
                            classDeclaration
                                    .getMethods()
                                    .size();

                    int fieldCount =
                            classDeclaration
                                    .getFields()
                                    .size();

                    JavaClassCoupling coupling =
                            calculateCoupling(
                                    classDeclaration,
                                    repositoryClasses);

                    double cohesion =
                            calculateCohesion(
                                    classDeclaration);

                    int sharedFieldPairs =
                            calculateSharedFieldPairs(
                                    classDeclaration);

                    int totalMethodPairs =
                            calculateTotalMethodPairs(
                                    classDeclaration);

                    return new JavaClassAnalysis(
                            name,
                            packageName,
                            loc,
                            methodCount,
                            fieldCount,

                            coupling.dependencyCount(),
                            coupling.dependencies(),

                            sharedFieldPairs,
                            totalMethodPairs,
                            cohesion
                    );
                })
                .toList();
    }

    // --------------------------------------------------
    // COUPLING
    // --------------------------------------------------

    public JavaClassCoupling calculateCoupling(
            ClassOrInterfaceDeclaration classDeclaration,
            Set<String> repositoryClasses) {

        Set<String> dependencies =
                new LinkedHashSet<>();

        classDeclaration
                .findAll(ClassOrInterfaceType.class)
                .forEach(type -> {

                    String typeName =
                            type.getNameAsString();

                    if (repositoryClasses.contains(
                            typeName)
                            && !typeName.equals(
                            classDeclaration
                                    .getNameAsString())) {

                        dependencies.add(typeName);
                    }
                });

        return new JavaClassCoupling(
                classDeclaration
                        .getNameAsString(),

                dependencies.size(),

                dependencies
                        .stream()
                        .toList()
        );
    }

    // --------------------------------------------------
    // COHESION
    // --------------------------------------------------

    private double calculateCohesion(
            ClassOrInterfaceDeclaration classDeclaration) {

        List<MethodDeclaration> methods =
                classDeclaration.getMethods();

        if (methods.size() < 2) {
            return 1.0;
        }

        int sharedFieldPairs =
                calculateSharedFieldPairs(
                        classDeclaration);

        int totalMethodPairs =
                calculateTotalMethodPairs(
                        classDeclaration);

        if (totalMethodPairs == 0) {
            return 1.0;
        }

        return (double) sharedFieldPairs
                / totalMethodPairs;
    }

    // --------------------------------------------------
    // FIND FIELDS USED BY A METHOD
    // --------------------------------------------------

    private Set<String> getUsedFields(
            ClassOrInterfaceDeclaration classDeclaration,
            MethodDeclaration method) {

        Set<String> classFields =
                classDeclaration
                        .getFields()
                        .stream()
                        .flatMap(field ->
                                field.getVariables()
                                        .stream())
                        .map(VariableDeclarator::
                                getNameAsString)
                        .collect(Collectors.toSet());

        return method
                .findAll(NameExpr.class)
                .stream()
                .map(NameExpr::getNameAsString)
                .filter(classFields::contains)
                .collect(Collectors.toSet());
    }

    // --------------------------------------------------
    // SHARED FIELD PAIRS
    // --------------------------------------------------

    private int calculateSharedFieldPairs(
            ClassOrInterfaceDeclaration classDeclaration) {

        List<MethodDeclaration> methods =
                classDeclaration.getMethods();

        int sharedFieldPairs = 0;

        for (int i = 0;
             i < methods.size();
             i++) {

            for (int j = i + 1;
                 j < methods.size();
                 j++) {

                Set<String> firstFields =
                        getUsedFields(
                                classDeclaration,
                                methods.get(i));

                Set<String> secondFields =
                        getUsedFields(
                                classDeclaration,
                                methods.get(j));

                boolean sharesField =
                        firstFields
                                .stream()
                                .anyMatch(
                                        secondFields::contains);

                if (sharesField) {
                    sharedFieldPairs++;
                }
            }
        }

        return sharedFieldPairs;
    }

    // --------------------------------------------------
    // TOTAL METHOD PAIRS
    // --------------------------------------------------

    private int calculateTotalMethodPairs(
            ClassOrInterfaceDeclaration classDeclaration) {

        int methodCount =
                classDeclaration
                        .getMethods()
                        .size();

        return methodCount
                * (methodCount - 1)
                / 2;
    }
}