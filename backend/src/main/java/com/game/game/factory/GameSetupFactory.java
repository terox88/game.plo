package com.game.game.factory;

import com.game.game.domain.*;

import java.util.*;

public class GameSetupFactory {

    private final LandTokenFactory landTokenFactory = new LandTokenFactory();
    private final RegionFactory regionFactory = new RegionFactory();
    private final ActionFieldFactory actionFieldFactory = new ActionFieldFactory();

    public GameState createGame(List<PlayerState> players) {

        if (players.isEmpty()) {
            throw new IllegalArgumentException("Players list cannot be empty");
        }

        int playersCount = players.size();

        List<RegionToken> tokens = landTokenFactory.create(playersCount);

        List<RegionState> regions = regionFactory.create();

        List<ActionField> actionFields = actionFieldFactory.create(playersCount);

        ViperGorge viperGorge = new ViperGorge();

        return GameState.builder()
                .players(players)
                .regions(regions)
                .actionFields(actionFields)
                .availableTokens(tokens)
                .viperGorge(viperGorge)
                .initiativeOrder(initOrder(players))
                .currentPlayerId(players.get(0).getPlayerId())
                .currentPhase(Phase.INITIATIVE)
                .deadSnow(0)
                .roundNumber(1)
                .stageNumber(1)
                .build();
    }

    private List<UUID> initOrder(List<PlayerState> players) {
        return players.stream()
                .map(PlayerState::getPlayerId)
                .toList();
    }
}