package com.repopulse.dto;

import java.util.List;

public record CircularDependency(
        List<String> cycle
) {}