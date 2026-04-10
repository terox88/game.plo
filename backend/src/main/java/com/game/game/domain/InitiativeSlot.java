package com.game.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Deque;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class InitiativeSlot {

    private int position;

    private Deque<UUID> players;

    public void addPlayer(UUID playerId) {
        players.addFirst(playerId);
    }

    public void removePlayer(UUID playerId) {
        players.remove(playerId);
    }
}
