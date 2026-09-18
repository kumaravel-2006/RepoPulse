package com.repopulse.dto;

import java.util.Map;
import java.util.Set;

public record DependencyGraph(
        Set<String> classes,
        Map<String, Set<String>> dependencies
) {}