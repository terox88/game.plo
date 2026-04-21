package com.game.game.domain.action;

import com.game.game.domain.GameState;
import com.game.game.domain.InfluenceMarker;
import com.game.game.domain.PlayerState;
import com.game.game.domain.RegionState;

import java.util.List;
import java.util.UUID;

public class InfluenceAction implements GameActionDomain {

    @Override
    public ActionResult start(ActionContext context) {
        return ActionResult.decision(List.of("INFLUENCE"));
    }

    @Override
    public ActionResult handleDecision(ActionContext context, PlayerDecision decision) {

        if (decision.getValue() == MakingChoice.PASS) {
            return ActionResult.finished();
        }

        InfluenceDecision influenceDecision = (InfluenceDecision) decision.getValue();

        GameState game = context.getGame();
        UUID playerId = context.getMarker().getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        List<Integer> regionNumbers = influenceDecision.regionNumbers();

        // =========================
        // WALIDACJA ILOŚCI
        // =========================

        if (regionNumbers.isEmpty() || regionNumbers.size() > 2) {
            throw new IllegalStateException("Invalid number of influence placements");
        }

        // =========================
        // WALIDACJA POPULATION
        // =========================

        if (player.getPopulation() < regionNumbers.size()) {
            throw new IllegalStateException("Not enough population");
        }

        // =========================
        // WALIDACJA REGIONÓW
        // =========================

        for (Integer regionNumber : regionNumbers) {

            RegionState region = game.getRegionByNumber(regionNumber);

            if (!region.isActive()) {
                throw new IllegalStateException("Cannot place influence in inactive region");
            }
            if(!region.isClosed()) {
                throw new IllegalStateException("Cannot place influence in closed region");
            }

            boolean hasInfluence = region.getInfluenceMarkers().stream()
                    .anyMatch(marker -> marker.getPlayerId().equals(playerId));

            boolean hasUnit = region.getUnits().stream()
                    .anyMatch(unit -> unit.getOwnerId().equals(playerId));

            if (!hasInfluence && !hasUnit) {
                throw new IllegalStateException(
                        "Cannot place influence in region without influence or unit"
                );
            }
        }

        // =========================
        // WYKONANIE
        // =========================

        for (Integer regionNumber : regionNumbers) {

            RegionState region = game.getRegionByNumber(regionNumber);

            region.getInfluenceMarkers().add(
                    new InfluenceMarker(playerId)
            );
        }

        player.spendPopulation(regionNumbers.size());

        return ActionResult.finished();
    }

    @Override
    public boolean isFinished(ActionContext context) {
        return true;
    }
}