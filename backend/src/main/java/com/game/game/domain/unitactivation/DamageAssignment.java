package com.game.game.domain.unitactivation;

import java.util.UUID;

public record DamageAssignment(
        AttackTargetType targetType,
        UUID targetId,
        int damage
) {}
