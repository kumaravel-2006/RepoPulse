package com.repopulse.dto;

import java.util.Map;
import java.util.Set;

public record PackageDependencyGraph(
        Set<String> packages,
        Map<String, Set<String>> dependencies
) {}