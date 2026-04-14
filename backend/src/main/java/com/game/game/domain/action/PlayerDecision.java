package com.game.game.domain.action;

public class PlayerDecision {

    private final Object value;

    public PlayerDecision(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
