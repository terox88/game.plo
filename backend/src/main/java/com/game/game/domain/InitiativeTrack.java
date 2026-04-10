package com.game.game.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiativeTrack {

    @Builder.Default
    private List<InitiativeSlot> slots = new ArrayList<>();

    public List<UUID> getTurnOrder() {
        return slots.stream()
                .sorted(Comparator.comparingInt(InitiativeSlot::getPosition))
                .flatMap(slot -> slot.getPlayers().stream())
                .toList();
    }

    public InitiativeSlot findPlayerSlot(UUID playerId) {
        return slots.stream()
                .filter(s -> s.getPlayers().contains(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Player not on track"));
    }
    public void movePlayer(UUID playerId, int steps) {

        InitiativeSlot current = findPlayerSlot(playerId);

        int newPosition = current.getPosition() + steps;

        current.removePlayer(playerId);

        InitiativeSlot target = slots.stream()
                .filter(s -> s.getPosition() == newPosition)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid target slot"));

        target.addPlayer(playerId);
    }
}
