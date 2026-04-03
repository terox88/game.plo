package com.game.game.application;

import com.game.game.application.action.*;
import com.game.game.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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

    private void nextPlayer(GameState game) {

        int index = game.getInitiativeOrder().indexOf(game.getCurrentPlayerId());

        int nextIndex = (index + 1) % game.getInitiativeOrder().size();

        game.setCurrentPlayerId(game.getInitiativeOrder().get(nextIndex));
    }
}