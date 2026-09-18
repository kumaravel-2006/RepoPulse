package com.repopulse.service;

import com.repopulse.dto.CodeSmell;
import com.repopulse.dto.JavaClassAnalysis;
import com.repopulse.dto.JavaMethodAnalysis;
import org.springframework.stereotype.Service;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.CatchClause;

import java.util.ArrayList;
import java.util.List;

@Service
public class CodeSmellService {

    private static final int LONG_METHOD_THRESHOLD = 50;
    private static final int LARGE_CLASS_THRESHOLD = 300;
    private static final int DEEP_NESTING_THRESHOLD = 4;
    private static final int TOO_MANY_PARAMETERS_THRESHOLD = 4;

    public List<CodeSmell> detectLongMethods(
            List<JavaMethodAnalysis> methods) {

        List<CodeSmell> smells = new ArrayList<>();

        for (JavaMethodAnalysis method : methods) {

            if (method.loc() > LONG_METHOD_THRESHOLD) {

                smells.add(
                        new CodeSmell(
                                "LONG_METHOD",
                                method.className(),
                                method.name(),
                                method.lineNumber(),
                                method.loc(),
                                LONG_METHOD_THRESHOLD,
                                "Method has "
                                        + method.loc()
                                        + " lines; threshold is "
                                        + LONG_METHOD_THRESHOLD
                                        + "."
                        )
                );
            }
        }

        return smells;
    }

    public List<CodeSmell> detectLargeClasses(
            List<JavaClassAnalysis> classes) {

        List<CodeSmell> smells = new ArrayList<>();

        for (JavaClassAnalysis classAnalysis : classes) {

            if (classAnalysis.loc() > LARGE_CLASS_THRESHOLD) {

                smells.add(
                        new CodeSmell(
                                "LARGE_CLASS",
                                classAnalysis.name(),
                                null,
                                0,
                                classAnalysis.loc(),
                                LARGE_CLASS_THRESHOLD,
                                "Class has "
                                        + classAnalysis.loc()
                                        + " lines; threshold is "
                                        + LARGE_CLASS_THRESHOLD
                                        + "."
                        )
                );
            }
        }

        return smells;
    }
    public List<CodeSmell> detectDeepNesting(List<JavaMethodAnalysis> methods) {
        List<CodeSmell> smells = new ArrayList<>();

        for (JavaMethodAnalysis method : methods) {
            if (method.maxNestingDepth() > DEEP_NESTING_THRESHOLD) {
                smells.add(new CodeSmell(
                        "DEEP_NESTING",
                        method.className(),
                        method.name(),
                        method.lineNumber(),
                        method.maxNestingDepth(),
                        DEEP_NESTING_THRESHOLD,
                        "Method has maximum nesting depth of "
                                + method.maxNestingDepth()
                                + "; threshold is "
                                + DEEP_NESTING_THRESHOLD
                                + "."
                ));
            }
        }

        return smells;
    }
    public List<CodeSmell> detectTooManyParameters(
            List<JavaMethodAnalysis> methods) {

        List<CodeSmell> smells = new ArrayList<>();

        for (JavaMethodAnalysis method : methods) {
            if (method.parameterCount() > TOO_MANY_PARAMETERS_THRESHOLD) {
                smells.add(new CodeSmell(
                        "TOO_MANY_PARAMETERS",
                        method.className(),
                        method.name(),
                        method.lineNumber(),
                        method.parameterCount(),
                        TOO_MANY_PARAMETERS_THRESHOLD,
                        "Method has "
                                + method.parameterCount()
                                + " parameters; threshold is "
                                + TOO_MANY_PARAMETERS_THRESHOLD
                                + "."
                ));
            }
        }

        return smells;
    }
    public List<CodeSmell> detectEmptyCatchBlocks(
            CompilationUnit compilationUnit) {

        List<CodeSmell> smells = new ArrayList<>();

        for (CatchClause catchClause :
                compilationUnit.findAll(CatchClause.class)) {

            if (!catchClause.getBody().getStatements().isEmpty()) {
                continue;
            }

            String className = catchClause
                    .findAncestor(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class)
                    .map(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration::getNameAsString)
                    .orElse("Unknown");

            String methodName = catchClause
                    .findAncestor(MethodDeclaration.class)
                    .map(MethodDeclaration::getNameAsString)
                    .orElse("Unknown");

            int lineNumber = catchClause
                    .getRange()
                    .map(range -> range.begin.line)
                    .orElse(0);

            smells.add(new CodeSmell(
                    "EMPTY_CATCH",
                    className,
                    methodName,
                    lineNumber,
                    0,
                    0,
                    "Catch block is empty."
            ));
        }

        return smells;
    }
    public List<CodeSmell> detectDuplicateCode(
            List<JavaMethodAnalysis> methods) {

        List<CodeSmell> smells = new ArrayList<>();

        for (int i = 0; i < methods.size(); i++) {

            JavaMethodAnalysis first = methods.get(i);

            if (first.normalizedBody().isEmpty()) {
                continue;
            }

            for (int j = i + 1; j < methods.size(); j++) {

                JavaMethodAnalysis second = methods.get(j);

                if (second.normalizedBody().isEmpty()) {
                    continue;
                }

                if (first.normalizedBody().equals(
                        second.normalizedBody())) {

                    smells.add(new CodeSmell(
                            "DUPLICATE_CODE",
                            first.className(),
                            first.name(),
                            first.lineNumber(),
                            first.loc(),
                            second.loc(),
                            "Method has duplicate code with "
                                    + second.className()
                                    + "."
                                    + second.name()
                                    + "."
                    ));

                    smells.add(new CodeSmell(
                            "DUPLICATE_CODE",
                            second.className(),
                            second.name(),
                            second.lineNumber(),
                            second.loc(),
                            first.loc(),
                            "Method has duplicate code with "
                                    + first.className()
                                    + "."
                                    + first.name()
                                    + "."
                    ));
                }
            }
        }

        return smells;
    }
}