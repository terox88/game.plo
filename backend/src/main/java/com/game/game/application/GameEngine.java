package com.game.game.application;

import com.game.game.application.action.AssignTokenToRegionAction;
import com.game.game.domain.*;

public class GameEngine {

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

    private void nextPlayer(GameState game) {

        int index = game.getInitiativeOrder().indexOf(game.getCurrentPlayerId());

        int nextIndex = (index + 1) % game.getInitiativeOrder().size();

        game.setCurrentPlayerId(game.getInitiativeOrder().get(nextIndex));
    }
}