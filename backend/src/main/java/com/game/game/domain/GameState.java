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
    private Map<UUID,Set<UUID>> setupInfluenceHistory;

    public RegionState getRegionByNumber(int number) {
        return regions.stream()
                .filter(r -> r.getNumber() == number)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + number));
    }

    public boolean isVanDykenInGame() {
        return players.stream().anyMatch(p -> p.getHero() == Hero.PIER);
    }
    public PlayerState findPlayer(UUID playerId) {

        if (playerId == null) {
            throw new IllegalArgumentException("PlayerId cannot be null");
        }

        return players.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }


}