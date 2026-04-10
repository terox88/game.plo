package com.game.game.application;

import com.game.game.application.action.*;
import com.game.game.domain.*;

import java.util.*;

public class GameEngine {

    private final Random random = new Random();

    // =========================================================
    // SETUP TOKENS
    // =========================================================

    public void assignToken(GameState game, AssignTokenToRegionAction action) {

        validatePhase(game, Phase.SETUP_TOKENS);
        validateTurn(game, action.getPlayerId());

        RegionState region = findRegion(game, action.getRegionId());

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
            game.setCurrentPhase(Phase.SETUP_UROCZYSKA);
        }
    }

    // =========================================================
    // THORN
    // =========================================================

    public void placeThorn(GameState game, PlaceThornAction action) {

        validatePhase(game, Phase.SETUP_THORN);
        validateTurn(game, action.getPlayerId());

        if (!game.isVanDykenInGame()) {
            throw new IllegalStateException("VanDyken not in game");
        }

        RegionState region = findRegion(game, action.getRegionId());

        if (!region.isActive()) {
            throw new IllegalStateException("Region not active");
        }

        if (region.hasThorn()) {
            throw new IllegalStateException("Region already has thorn");
        }

        region.getFeatures().add(RegionFeature.THORN);

        game.setCurrentPhase(Phase.SETUP_UROCZYSKA);
    }

    // =========================================================
    // UROCZYSKA
    // =========================================================

    public void distributeUroczyska(GameState game) {

        validatePhase(game, Phase.SETUP_UROCZYSKA);

        var validRegions = game.getRegions().stream()
                .filter(RegionState::isActive)
                .filter(r -> !r.hasThorn())
                .toList();

        if (validRegions.size() < 4) {
            throw new IllegalStateException("Not enough valid regions");
        }

        var shuffled = new ArrayList<>(validRegions);
        Collections.shuffle(shuffled, random);

        for (int i = 0; i < 4; i++) {
            shuffled.get(i).getUroczyska().add(
                    Uroczysko.builder()
                            .id(i)
                            .flipped(false)
                            .build()
            );
        }

        game.setCurrentPhase(Phase.SETUP_INFLUENCE_1);
    }

    // =========================================================
    // INFLUENCE (SETUP)
    // =========================================================

    public void placeInfluence(GameState game, PlaceInfluenceAction action) {

        validatePhase(game, Phase.SETUP_INFLUENCE_1, Phase.SETUP_INFLUENCE_2);
        validateTurn(game, action.getPlayerId());

        RegionState region = findRegion(game, action.getRegionId());

        if (!region.isActive()) {
            throw new IllegalStateException("Region not active");
        }

        PlayerState player = game.findPlayer(action.getPlayerId());

        var history = game.getSetupInfluenceHistory()
                .computeIfAbsent(player.getPlayerId(), k -> new HashSet<>());

        if (history.contains(region.getId())) {
            throw new IllegalStateException("Cannot place twice in same region");
        }

        player.useInfluenceMarker();

        region.getInfluenceMarkers().add(
                new InfluenceMarker(player.getPlayerId())
        );

        history.add(region.getId());

        nextPlayer(game);
        updateInfluencePhase(game);
    }

    private void updateInfluencePhase(GameState game) {

        int players = game.getPlayers().size();

        int totalPlaced = game.getSetupInfluenceHistory().values().stream()
                .mapToInt(Set::size)
                .sum();

        if (game.getCurrentPhase() == Phase.SETUP_INFLUENCE_1 &&
                totalPlaced == players) {

            game.setCurrentPhase(Phase.SETUP_INFLUENCE_2);
            return;
        }

        if (game.getCurrentPhase() == Phase.SETUP_INFLUENCE_2 &&
                totalPlaced == players * 2) {

            game.getSetupInfluenceHistory().clear(); // cleanup
            game.setCurrentPhase(Phase.INITIATIVE);
        }
    }

    // =========================================================
    // INITIATIVE PHASE
    // =========================================================

    public void advanceInitiative(GameState game, AdvanceInitiativeAction action) {

        validatePhase(game, Phase.INITIATIVE);
        validateTurn(game, action.getPlayerId());

        PlayerState player = game.findPlayer(action.getPlayerId());

        int steps = action.getSteps();

        if (steps == 2) {
            player.useDoubleMove();
        } else if (steps == 1) {
            player.moveOne();
        } else {
            throw new IllegalArgumentException("Invalid steps");
        }

        game.getInitiativeTrack().movePlayer(player.getPlayerId(), steps);

        game.updateGlobalProgress(player);

        nextPlayer(game);
    }
    // =========================================================
    // REPUTATION
    // =========================================================
    public void changeReputation(GameState game, UUID playerId, int delta) {

        PlayerState player = game.findPlayer(playerId);

        int oldLevel = player.getReputation();
        int newLevel = oldLevel + delta;

        // 🔒 walidacje domenowe

       validateReputationChange(oldLevel, newLevel);

       if (oldLevel == newLevel) {
            return;
        }

        ReputationTrack track = game.getReputationTrack();

        // usuń z poprzedniego slotu
        track.getSlot(oldLevel).remove(playerId);

        if (delta > 0) {
            // 🔻 pogorszenie → NA WIERZCH
            track.getSlot(newLevel).addOnTop(playerId);
        } else {
            // 🔺 poprawa → NA SPÓD
            track.getSlot(newLevel).addAtBottom(playerId);
        }

        player.setReputation(newLevel);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private RegionState findRegion(GameState game, UUID regionId) {
        return game.getRegions().stream()
                .filter(r -> r.getId().equals(regionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));
    }

    private void validateTurn(GameState game, UUID playerId) {
        if (!game.getCurrentPlayerId().equals(playerId)) {
            throw new IllegalStateException("Not this player's turn");
        }
    }

    private void validatePhase(GameState game, Phase... phases) {
        if (Arrays.stream(phases).noneMatch(p -> p == game.getCurrentPhase())) {
            throw new IllegalStateException("Invalid phase: " + game.getCurrentPhase());
        }
    }

    private void nextPlayer(GameState game) {

        List<UUID> order = game.getInitiativeTrack().getTurnOrder();

        int index = order.indexOf(game.getCurrentPlayerId());

        if (index == -1) {
            throw new IllegalStateException("Player not on initiative track");
        }

        int nextIndex = (index + 1) % order.size();

        game.setCurrentPlayerId(order.get(nextIndex));
    }

    private void validateReputationChange(int oldLevel, int newLevel) {

        if (newLevel > 10) {
            throw new IllegalStateException("Cannot go beyond reputation level 9");
        }

        // ❗ blokada powrotu na 0
        if (newLevel == 0 && oldLevel != 0) {
            throw new IllegalStateException("Cannot return to Yin-Yang");
        }

        // ❗ dodatkowo: nie schodzimy poniżej 0
        if (newLevel < 0) {
            throw new IllegalStateException("Invalid reputation level");
        }
    }
}