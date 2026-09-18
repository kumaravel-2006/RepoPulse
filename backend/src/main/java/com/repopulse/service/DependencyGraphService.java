package com.repopulse.service;

import com.repopulse.dto.DependencyGraph;
import com.repopulse.dto.JavaClassAnalysis;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DependencyGraphService {

    public DependencyGraph buildClassDependencyGraph(
            List<JavaClassAnalysis> classes) {

        Set<String> classNames = new HashSet<>();
        Map<String, Set<String>> dependencies = new HashMap<>();

        for (JavaClassAnalysis classAnalysis : classes) {

            String className = classAnalysis.name();

            classNames.add(className);

            dependencies.put(
                    className,
                    new HashSet<>(classAnalysis.dependencies())
            );
        }

        return new DependencyGraph(
                classNames,
                dependencies
        );
    }


}