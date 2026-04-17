package com.game.game.domain.action;

import java.util.List;

public record MoveDecision(
        List<SingleMove> moves
) {}
