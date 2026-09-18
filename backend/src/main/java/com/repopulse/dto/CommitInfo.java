package com.repopulse.dto;

import java.time.Instant;

public record CommitInfo(
        String hash,
        String author,
        String message,
        Instant timestamp
) {
}