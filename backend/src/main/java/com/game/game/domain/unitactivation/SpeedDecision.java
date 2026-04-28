package com.game.game.domain.unitactivation;

import java.util.List;
import java.util.UUID;

public record SpeedDecision(
        UUID playerId,
        List<SpeedMove> moves
) {}
