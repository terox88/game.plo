package com.game.game.domain.action;

import java.util.List;
import java.util.UUID;

public record MountainDecision(
        int fromRegionNumber,
        int toRegionNumber,
        int influenceCount,
        List<UUID> unitIds
) {}
