package com.game.game.domain.unitactivation;

import java.util.List;
import java.util.UUID;

public record DrakkarDecision(
        UUID playerId,
        UUID fromRegionId,
        UUID toRegionId,
        List<UUID> unitIds,
        int influenceMarkersAmount
) {}
