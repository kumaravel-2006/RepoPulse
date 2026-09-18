package com.repopulse.service;

import com.repopulse.dto.ArchitectureViolation;
import com.repopulse.dto.PackageDependencyGraph;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ArchitectureViolationService {

    public List<ArchitectureViolation> detectViolations(
            PackageDependencyGraph graph) {

        List<ArchitectureViolation> violations =
                new ArrayList<>();

        Map<String, Set<String>> dependencies =
                graph.dependencies();

        for (Map.Entry<String, Set<String>> entry :
                dependencies.entrySet()) {

            String sourcePackage = entry.getKey();

            for (String targetPackage : entry.getValue()) {

                if (isViolation(sourcePackage, targetPackage)) {

                    violations.add(
                            new ArchitectureViolation(
                                    sourcePackage,
                                    targetPackage,
                                    "Package depends on a higher architectural layer"
                            )
                    );
                }
            }
        }

        return violations;
    }

    private boolean isViolation(
            String sourcePackage,
            String targetPackage) {

        int sourceLayer = getLayer(sourcePackage);
        int targetLayer = getLayer(targetPackage);

        return sourceLayer < targetLayer;
    }

    private int getLayer(String packageName) {

        if (packageName.endsWith(".ui")) {
            return 3;
        }

        if (packageName.endsWith(".services")) {
            return 2;
        }

        if (packageName.endsWith(".entities")) {
            return 1;
        }

        return 0;
    }
}