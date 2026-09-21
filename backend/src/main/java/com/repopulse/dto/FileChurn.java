package com.repopulse.dto;

public record FileChurn(
        String file,
        int changeCount
) {}