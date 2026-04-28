package com.game.game.domain.unitactivation;

import java.util.List;
import java.util.UUID;

public record RegionAttackDecision(
        UUID regionId,
        List<DamageAssignment> damageAssignments
) {}
