package com.repopulse.dto;

import java.util.List;

public record JavaFileAnalysis(
        String file,
        int loc,
        List<String> classes,
        List<JavaMethodAnalysis> methods,
        List<JavaClassAnalysis> classMetrics,
        List<CodeSmell> codeSmells,
        String error
) {
}