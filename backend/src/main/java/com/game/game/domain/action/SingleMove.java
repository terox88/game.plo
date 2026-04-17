package com.game.game.domain.action;

import java.util.UUID;

public record SingleMove(
        UUID fromRegionId,
        UUID toRegionId,
        UUID unitId,
        UUID influenceOwnerId
) {}
