package com.game.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class InitiativeSlot {

    private int position;

    private List<UUID> players;

    public void addPlayer(UUID playerId) {
        players.add(playerId);
    }

    public void removePlayer(UUID playerId) {
        players.remove(playerId);
    }
}
