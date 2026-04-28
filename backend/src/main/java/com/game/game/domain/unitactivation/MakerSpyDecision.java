package com.game.game.domain.unitactivation;

import com.game.game.domain.AbilitiesType;

import java.util.UUID;

public record MakerSpyDecision(
        UUID playerId,
        AbilitiesType firstAbility,
        AbilitiesType secondAbility
) {}
