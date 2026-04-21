package com.game.game.domain.action;

import java.util.List;

public record ViperGorgeDecision(
        List<ResourceType> takenResources,
        List<ExchangeDecision> exchanges
) {}
