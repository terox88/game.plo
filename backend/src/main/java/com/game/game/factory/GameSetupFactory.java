package com.game.game.factory;

import com.game.game.domain.*;

import java.util.*;

public class GameSetupFactory {

    private final LandTokenFactory landTokenFactory = new LandTokenFactory();
    private final RegionFactory regionFactory = new RegionFactory();
    private final ActionFieldFactory actionFieldFactory = new ActionFieldFactory();
    private final Random random = new Random();

    public GameState createGame(List<PlayerState> players) {

        if (players.isEmpty()) {
            throw new IllegalArgumentException("Players list cannot be empty");
        }

        int playersCount = players.size();
        InitiativeTrack track = createInitiativeTrack(players);
        ReputationTrack reputationTrack = createReputationTrack(players);

        List<RegionToken> tokens = landTokenFactory.create(playersCount);
        Collections.shuffle(tokens, random);

        List<RegionState> regions = regionFactory.create();

        List<ActionField> actionFields = actionFieldFactory.create(playersCount);

        ViperGorge viperGorge = new ViperGorge();

        return GameState.builder()
                .players(players)
                .regions(regions)
                .actionFields(actionFields)
                .availableTokens(tokens)
                .viperGorge(viperGorge)
                .initiativeTrack(track)
                .reputationTrack(reputationTrack)
                .currentPlayerId(players.get(0).getPlayerId())
                .currentPhase(Phase.SETUP_TOKENS)
                .deadSnow(0)
                .stageLast(1)
                .roundLast(1)
                .build();
    }

    private List<UUID> initOrder(List<PlayerState> players) {
        return players.stream()
                .map(PlayerState::getPlayerId)
                .toList();
    }

    private InitiativeTrack createInitiativeTrack(List<PlayerState> players) {

        List<InitiativeSlot> slots = new ArrayList<>();


        for (int i = 0; i < 10; i++) {
            slots.add(new InitiativeSlot(i, new ArrayDeque<>()));
        }

        InitiativeSlot startSlot = slots.get(0);

        players.forEach(p -> startSlot.getPlayers().addLast(p.getPlayerId()));

        return InitiativeTrack.builder()
                .slots(slots)
                .build();
    }

    private ReputationTrack createReputationTrack(List<PlayerState> players) {

        List<ReputationSlot> slots = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            slots.add(new ReputationSlot(i, new ArrayDeque<>()));
        }

        // start: wszyscy na poziomie 0
        players.forEach(p -> {
            slots.get(0).addOnTop(p.getPlayerId());
            p.setReputation(0);
        });

        return ReputationTrack.builder()
                .slots(slots)
                .build();
    }
}