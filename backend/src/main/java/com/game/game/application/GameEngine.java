
package com.game.game.application;

import com.game.game.application.command.*;
import com.game.game.domain.*;

import java.util.*;
import java.util.stream.IntStream;

public class GameEngine {

    private final Random random = new Random();

    // =========================================================
    // 🔥 NOWE: START INITIATIVE PHASE
    // =========================================================

    public void startInitiativePhase(GameState game) {
        game.setInitiativeTurnOrder(new ArrayList<>(game.getCurrentTurnOrder()));
        game.setCurrentPlayerId(game.getInitiativeTurnOrder().get(0));
    }

    // =========================================================
    // SETUP TOKENS
    // =========================================================

    public void assignToken(GameState game, AssignTokenToRegionAction action) {

        validatePhase(game, Phase.SETUP_TOKENS);
        validateTurn(game, action);

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
        validateTurn(game, action);

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
        validateTurn(game, action);

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

            game.getSetupInfluenceHistory().clear();
            game.setCurrentPhase(Phase.INITIATIVE);
        }
    }

    // =========================================================
    // INITIATIVE PHASE
    // =========================================================

    public void advanceInitiative(GameState game, AdvanceInitiativeAction action) {

        validatePhase(game, Phase.INITIATIVE);
        validateTurn(game, action);

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

        // 🔥 NOWE
        if (isLastPlayerInInitiative(game)) {
            endInitiativePhase(game);
        }
    }

    // 🔥 NOWE
    private boolean isLastPlayerInInitiative(GameState game) {
        List<UUID> order = game.getInitiativeTurnOrder();
        return game.getCurrentPlayerId().equals(order.get(0));
    }

    // 🔥 NOWE
    private void endInitiativePhase(GameState game) {
        game.setCurrentTurnOrder(game.calculateTurnOrder());
        game.setCurrentPhase(Phase.PLANNING_ORDER);
        game.setCurrentPlayerId(game.getCurrentTurnOrder().get(0));
    }

    // =========================================================
    // REPUTATION
    // =========================================================

    public void changeReputation(GameState game, UUID playerId, int delta) {

        PlayerState player = game.findPlayer(playerId);

        int oldLevel = player.getReputation();
        int newLevel = oldLevel + delta;

        validateReputationChange(oldLevel, newLevel);

        if (oldLevel == newLevel) {
            return;
        }

        ReputationTrack track = game.getReputationTrack();

        track.getSlot(oldLevel).remove(playerId);

        if (delta > 0) {
            track.getSlot(newLevel).addOnTop(playerId);
        } else {
            track.getSlot(newLevel).addAtBottom(playerId);
        }

        player.setReputation(newLevel);
    }

    // =========================================================
    // PLANING
    // =========================================================

    public void assignActionOrder(GameState game, AssignActionOrderAction action) {

        validatePhase(game, Phase.PLANNING_ORDER);
        validateTurn(game, action);

        if (action.getOrder() < 1 || action.getOrder() > 5) {
            throw new IllegalArgumentException("Order must be between 1 and 5");
        }

        if (game.getUsedOrderNumbers().contains(action.getOrder())) {
            throw new IllegalStateException("Order number already used");
        }

        if (game.getAssignedFields().contains(action.getField())) {
            throw new IllegalStateException("Field already assigned");
        }

        game.getActionOrderAssignments().put(action.getField(), action.getOrder());
        game.getUsedOrderNumbers().add(action.getOrder());
        game.getAssignedFields().add(action.getField());

        autoAssignLastField(game);

        nextPlayer(game);

        if (game.getActionOrderAssignments().size() == 5) {
            game.setCurrentPhase(Phase.PLANNING_ACTIONS);
        }
    }

    public void placeActionMarker(GameState game, PlaceActionMarkerOnFieldAction action) {

        validatePhase(game, Phase.PLANNING_ACTIONS);
        skipPlayersWithoutMarkers(game);
        validateTurn(game, action);

        PlayerState player = game.findPlayer(action.getPlayerId());

        if (player.getAvailableActionMarkers() <= 0) {
            throw new IllegalStateException("No available markers");
        }

        ActionField field = findField(game, action.getField());
        field.placeMarker(new ActionMarker(action.getPlayerId(), field.getType()));

        player.useActionMarker();

        nextPlayerSkippingFinished(game);
        if (allPlayersFinishedPlanning(game)) {
            game.setCurrentPhase(Phase.ACTION);
            return;
        }
    }

    public void placeOnViperGorge(GameState game, PlaceActionMarkerOnViperGorgeAction action) {

        validatePhase(game, Phase.PLANNING_ACTIONS);
        validateTurn(game, action);

        PlayerState player = game.findPlayer(action.getPlayerId());

        if (player.getAvailableActionMarkers() <= 0) {
            throw new IllegalStateException("No available markers");
        }

        if (player.getAvailableInfluenceMarkers() <= 0) {
            throw new IllegalStateException("No available influence markers");
        }

        game.getViperGorge().addActionMarker(new ActionMarker(action.getPlayerId(),ActionFieldType.VIPER_GORGE));

        player.useActionMarker();
        game.getViperGorge().addInfluenceMarker(new InfluenceMarker(action.getPlayerId()));
        player.useInfluenceMarker();

        nextPlayerSkippingFinished(game);
        if (allPlayersFinishedPlanning(game)) {
            game.setCurrentPhase(Phase.ACTION);
            return;
        }
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

    private void validateTurn(GameState game, GameActionCommand command) {
        if (!game.getCurrentPlayerId().equals(command.getPlayerId())) {
            throw new IllegalStateException("Not this player's turn");
        }
    }

    private void validatePhase(GameState game, Phase... phases) {
        if (Arrays.stream(phases).noneMatch(p -> p == game.getCurrentPhase())) {
            throw new IllegalStateException("Invalid phase: " + game.getCurrentPhase());
        }
    }

    private void nextPlayer(GameState game) {

        List<UUID> order;

        if (game.getCurrentPhase() == Phase.INITIATIVE) {
            order = game.getInitiativeTurnOrder();
        } else {
            order = game.getCurrentTurnOrder();
        }

        if (order == null || order.isEmpty()) {
            throw new IllegalStateException("Turn order not initialized");
        }

        int index = order.indexOf(game.getCurrentPlayerId());

        if (index == -1) {
            throw new IllegalStateException("Player not on initiative track");
        }

        int nextIndex = (index + 1) % order.size();

        game.setCurrentPlayerId(order.get(nextIndex));
    }
    private void autoAssignLastField(GameState game) {

        // musi być dokładnie 4 przypisane
        if (game.getAssignedFields().size() != 4) {
            return;
        }

        // 🔍 znajdź brakujące pole
        ActionFieldType remainingField = Arrays.stream(ActionFieldType.values())
                .filter(f -> !game.getAssignedFields().contains(f))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No remaining field to assign"));

        // 🔍 znajdź brakującą liczbę
        int remainingOrder = IntStream.rangeClosed(1, 5)
                .filter(o -> !game.getUsedOrderNumbers().contains(o))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No remaining order number"));

        // ✅ przypisz
        game.getActionOrderAssignments().put(remainingField, remainingOrder);

        // 🔥 (opcjonalnie, ale lepiej zachować spójność)
        game.getAssignedFields().add(remainingField);
        game.getUsedOrderNumbers().add(remainingOrder);
    }
    private void nextPlayerSkippingFinished(GameState game) {

        List<UUID> order = game.getCurrentTurnOrder();

        int index = order.indexOf(game.getCurrentPlayerId());

        for (int i = 1; i <= order.size(); i++) {
            UUID next = order.get((index + i) % order.size());

            PlayerState p = game.findPlayer(next);

            if (p.getAvailableActionMarkers() > 0) {
                game.setCurrentPlayerId(next);
                return;
            }
        }
    }

    private void skipPlayersWithoutMarkers(GameState game) {

        PlayerState current = game.findPlayer(game.getCurrentPlayerId());

        if (current.getAvailableActionMarkers() > 0) {
            return;
        }

        nextPlayerSkippingFinished(game);
    }

    private boolean allPlayersFinishedPlanning(GameState game) {
        return game.getPlayers().stream()
                .allMatch(p -> p.getAvailableActionMarkers() == 0);
    }

    private ActionField findField(GameState game, ActionFieldType fieldType) {

        if (fieldType == null) {
            throw new IllegalArgumentException("Action field type cannot be null");
        }

        return game.getActionFields().stream()
                .filter(f -> f.getType() == fieldType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action field not found: " + fieldType));
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

