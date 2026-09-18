package com.repopulse.service;

import com.repopulse.dto.CircularDependency;
import com.repopulse.dto.DependencyGraph;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CircularDependencyService {

    public List<CircularDependency> detectCycles(
            DependencyGraph graph) {

        List<CircularDependency> cycles = new ArrayList<>();
        Set<String> uniqueCycles = new HashSet<>();

        for (String startClass : graph.classes()) {

            List<String> path = new ArrayList<>();
            Set<String> visited = new HashSet<>();

            findCycle(
                    startClass,
                    startClass,
                    graph.dependencies(),
                    path,
                    visited,
                    cycles,
                    uniqueCycles
            );
        }

        return cycles;
    }

    private void findCycle(
            String startClass,
            String currentClass,
            Map<String, Set<String>> dependencies,
            List<String> path,
            Set<String> visited,
            List<CircularDependency> cycles,
            Set<String> uniqueCycles) {

        path.add(currentClass);
        visited.add(currentClass);

        for (String dependency : dependencies.getOrDefault(
                currentClass,
                Set.of())) {

            // Cycle found
            if (dependency.equals(startClass)) {

                List<String> cycle =
                        new ArrayList<>(path);

                cycle.add(startClass);

                String cycleKey = createCycleKey(cycle);

                if (uniqueCycles.add(cycleKey)) {

                    cycles.add(
                            new CircularDependency(cycle)
                    );
                }

                continue;
            }

            // Continue traversal
            if (!visited.contains(dependency)) {

                findCycle(
                        startClass,
                        dependency,
                        dependencies,
                        path,
                        visited,
                        cycles,
                        uniqueCycles
                );
            }
        }

        path.remove(path.size() - 1);
        visited.remove(currentClass);
    }

    private String createCycleKey(List<String> cycle) {

        List<String> nodes =
                new ArrayList<>(cycle.subList(0, cycle.size() - 1));

        nodes.sort(String::compareTo);

        return String.join("->", nodes);
    }
}