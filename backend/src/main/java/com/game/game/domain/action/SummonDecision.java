package com.game.game.domain.action;

import java.util.Map;

public record SummonDecision(
        int level,
        Map<Integer, Integer> regionToAmount
) {}
