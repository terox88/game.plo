package com.game.game.domain.unitactivation;

import java.util.List;
import java.util.UUID;

public record AttackDecision(
        UUID playerId,
        List<RegionAttackDecision> regionAttacks
) {}
