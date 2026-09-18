package com.repopulse.service;

import com.repopulse.dto.JavaClassAnalysis;
import com.repopulse.dto.PackageDependencyGraph;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PackageDependencyGraphService {

    public PackageDependencyGraph buildPackageDependencyGraph(
            List<JavaClassAnalysis> classes) {

        Set<String> packages = new HashSet<>();
        Map<String, Set<String>> dependencies = new HashMap<>();

        Map<String, String> classToPackage = new HashMap<>();

        // Step 1: Map every class to its package
        for (JavaClassAnalysis classAnalysis : classes) {

            classToPackage.put(
                    classAnalysis.name(),
                    classAnalysis.packageName()
            );

            packages.add(classAnalysis.packageName());
        }

        // Step 2: Convert class dependencies into package dependencies
        for (JavaClassAnalysis classAnalysis : classes) {

            String sourcePackage = classAnalysis.packageName();

            dependencies.putIfAbsent(
                    sourcePackage,
                    new HashSet<>()
            );

            for (String dependency : classAnalysis.dependencies()) {

                String targetPackage = classToPackage.get(dependency);

                if (targetPackage == null) {
                    continue;
                }

                if (!sourcePackage.equals(targetPackage)) {
                    dependencies.get(sourcePackage)
                            .add(targetPackage);
                }
            }
        }

        // Step 3: Make sure packages with no outgoing
        // dependencies are also represented
        for (String packageName : packages) {
            dependencies.putIfAbsent(
                    packageName,
                    new HashSet<>()
            );
        }

        return new PackageDependencyGraph(
                packages,
                dependencies
        );
    }
}