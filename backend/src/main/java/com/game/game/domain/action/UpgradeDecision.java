package com.game.game.domain.action;

import com.game.game.domain.AbilitiesType;

public record UpgradeDecision(
        int level,
        AbilitiesType abilityToAdd,
        AbilitiesType abilityToReplace // null jeśli ADD
) {}
