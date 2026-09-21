package com.repopulse.analysis.git;

public record FileOwnership(
        String file,
        String ownerName,
        String ownerEmail,
        int ownerChangeCount,
        int totalChanges,
        double ownershipPercentage
) {
}