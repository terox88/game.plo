package com.game.game.domain.vuko;

import java.util.UUID;

public record VukoKillDecision(
        UUID playerId,
        UUID unitId
) {}
