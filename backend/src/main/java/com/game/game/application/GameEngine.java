package com.game.game.application;

import com.game.game.application.action.*;
import com.game.game.domain.*;

import java.util.*;

public class GameEngine {

    private final Random random = new Random();

    public void assignToken(GameState game, AssignTokenToRegionAction action) {


        if (game.getCurrentPhase() != Phase.SETUP_TOKENS) {
            throw new IllegalStateException("Not in token setup phase");
        }

        if (!game.getCurrentPlayerId().equals(action.getPlayerId())) {
            throw new IllegalStateException("Not this player's turn");
        }


        RegionState region = game.getRegions().stream()
                .filter(r -> r.getId().equals(action.getRegionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        if (region.isActive()) {
            throw new IllegalStateException("Region already active");
        }

        RegionToken token = game.getAvailableTokens().stream()
                .filter(t -> t.getId().equals(action.getTokenId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Token not available"));


        region.setLandToken(token);
        region.getFeatures().add(RegionFeature.IN_GAME);
        game.getAvailableTokens().remove(token);

        if (game.getAvailableTokens().isEmpty()) {
            moveToNextPhaseAfterTokens(game);
            return;
        }


        nextPlayer(game);
    }

    private void moveToNextPhaseAfterTokens(GameState game) {

        if (game.isVanDykenInGame()) {
            game.setCurrentPhase(Phase.SETUP_THORN);
        } else {
            game.setCurrentPhase(Phase.SETUP_INFLUENCE_1);
        }
    }

    public void placeThorn(GameState game, PlaceThornAction action) {

        if (game.getCurrentPhase() != Phase.SETUP_THORN) {
            throw new IllegalStateException("Not in thorn setup phase");
        }

        if (!game.isVanDykenInGame()) {
            throw new IllegalStateException("VanDyken not in game");
        }

        if (!game.getCurrentPlayerId().equals(action.getPlayerId())) {
            throw new IllegalStateException("Not this player's turn");
        }

        RegionState region = game.getRegions().stream()
                .filter(r -> r.getId().equals(action.getRegionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        if (!region.isActive()) {
            throw new IllegalStateException("Region not active");
        }

        if (region.hasThorn()) {
            throw new IllegalStateException("Region already has thorn");
        }

        region.getFeatures().add(RegionFeature.THORN);

        game.setCurrentPhase(Phase.SETUP_UROCZYSKA);
    }

    public void distributeUroczyska(GameState game) {

        if (game.getCurrentPhase() != Phase.SETUP_UROCZYSKA) {
            throw new IllegalStateException("Not in uroczyska setup phase");
        }

        var validRegions = game.getRegions().stream()
                .filter(RegionState::isActive)
                .filter(r -> !r.hasThorn())
                .toList();

        if (validRegions.size() < 4) {
            throw new IllegalStateException("Not enough valid regions for uroczyska");
        }

        var shuffled = new ArrayList<>(validRegions);
        Collections.shuffle(shuffled, random);

        for (int i = 0; i < 4; i++) {
            RegionState region = shuffled.get(i);

            region.getUroczyska().add(
                    Uroczysko.builder()
                            .id(i)
                            .flipped(false)
                            .build()
            );
        }

        game.setCurrentPhase(Phase.SETUP_INFLUENCE_1);
    }

    public void placeInfluence(GameState game, PlaceInfluenceAction action) {

        // 🔒 1. faza
        if (game.getCurrentPhase() != Phase.SETUP_INFLUENCE_1 &&
                game.getCurrentPhase() != Phase.SETUP_INFLUENCE_2) {
            throw new IllegalStateException("Not in influence setup phase");
        }

        // 👤 2. tura
        if (!game.getCurrentPlayerId().equals(action.getPlayerId())) {
            throw new IllegalStateException("Not this player's turn");
        }

        // 🌍 3. region
        RegionState region = game.getRegions().stream()
                .filter(r -> r.getId().equals(action.getRegionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        if (!region.isActive()) {
            throw new IllegalStateException("Region not active");
        }

        // ❗ 4. historia gracza
        var history = game.getSetupInfluenceHistory()
                .computeIfAbsent(action.getPlayerId(), k -> new HashSet<>());

        if (history.contains(region.getId())) {
            throw new IllegalStateException("Cannot place influence twice in same region");
        }

        // 👤 pobierz gracza
        PlayerState player = game.findPlayer(action.getPlayerId());

        // ❗ czy ma dostępne markery
        if (player.getAvailableInfluenceMarkers() <= 0) {
            throw new IllegalStateException("No available influence markers");
        }

        // 🔥 5. wykonanie
        region.getInfluenceMarkers().add(
                new InfluenceMarker(action.getPlayerId())
        );
        player.useInfluenceMarker();

        history.add(region.getId());

        // 🔄 6. przejście dalej
        nextPlayer(game);

        // 🔁 7. zmiana rundy/fazy
        updateInfluencePhase(game);
    }

    private void nextPlayer(GameState game) {

        int index = game.getInitiativeOrder().indexOf(game.getCurrentPlayerId());

        int nextIndex = (index + 1) % game.getInitiativeOrder().size();

        game.setCurrentPlayerId(game.getInitiativeOrder().get(nextIndex));
    }
    private void updateInfluencePhase(GameState game) {

        int totalPlayers = game.getPlayers().size();
        int totalPlaced = game.getSetupInfluenceHistory().values().stream()
                .mapToInt(Set::size)
                .sum();

        // 🧠 każda runda = liczba graczy
        if (game.getCurrentPhase() == Phase.SETUP_INFLUENCE_1 &&
                totalPlaced == totalPlayers) {

            game.setCurrentPhase(Phase.SETUP_INFLUENCE_2);
            return;
        }

        if (game.getCurrentPhase() == Phase.SETUP_INFLUENCE_2 &&
                totalPlaced == totalPlayers * 2) {

            game.setCurrentPhase(Phase.INITIATIVE);
        }
    }
}