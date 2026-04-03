package com.game.game.domain;

import lombok.*;

import java.util.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameState {

    private List<PlayerState> players;
    private List<RegionState> regions;
    private List<ActionField> actionFields;
    private List<RegionToken> availableTokens;
    private ViperGorge viperGorge;

    private List<UUID> initiativeOrder;
    private UUID currentPlayerId;

    private Phase currentPhase;

    private int deadSnow;
    private int roundNumber;
    private int stageNumber;

    public RegionState getRegionByNumber(int number) {
        return regions.stream()
                .filter(r -> r.getNumber() == number)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + number));
    }

    public boolean isVanDykenInGame() {
        return players.stream().anyMatch(p -> p.getHero() == Hero.PIER);
    }
}