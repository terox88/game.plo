package com.game.game.domain.action;

public record ExchangeDecision(
        ResourceType first,
        ResourceType second,
        ResourceType result
) {}
