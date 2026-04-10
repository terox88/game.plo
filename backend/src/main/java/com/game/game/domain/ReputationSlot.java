package com.game.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Deque;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReputationSlot {

    private int level;
    private Deque<UUID> players;

    public void addOnTop(UUID playerId) {
        players.addFirst(playerId);
    }

    public void addAtBottom(UUID playerId) {
        players.addLast(playerId);
    }

    public void remove(UUID playerId) {
        players.remove(playerId);
    }
}
