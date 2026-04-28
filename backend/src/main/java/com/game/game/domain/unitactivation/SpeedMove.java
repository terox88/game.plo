package com.game.game.domain.unitactivation;

import java.util.UUID;

public record SpeedMove(
        UUID fromRegionId,
        UUID toRegionId,
        UUID unitId
) {}
