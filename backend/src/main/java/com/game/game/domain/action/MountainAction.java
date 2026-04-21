package com.game.game.domain.action;

import com.game.game.domain.GameState;
import com.game.game.domain.InfluenceMarker;
import com.game.game.domain.PlayerState;
import com.game.game.domain.RegionState;
import com.game.game.domain.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MountainAction implements GameActionDomain {

    @Override
    public ActionResult start(ActionContext context) {
        return ActionResult.decision(List.of("MOUNTAIN"));
    }

    @Override
    public ActionResult handleDecision(ActionContext context, PlayerDecision decision) {

        if (decision.getValue() == MakingChoice.PASS) {
            return ActionResult.finished();
        }

        MountainDecision mountainDecision = (MountainDecision) decision.getValue();

        GameState game = context.getGame();
        UUID playerId = context.getMarker().getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        RegionState from = game.getRegionByNumber(
                mountainDecision.fromRegionNumber()
        );

        RegionState to = game.getRegionByNumber(
                mountainDecision.toRegionNumber()
        );

        int influenceCount = mountainDecision.influenceCount();
        List<UUID> unitIds = mountainDecision.unitIds();

        // =========================
        // WALIDACJA BASIC
        // =========================

        if (mountainDecision.fromRegionNumber() == mountainDecision.toRegionNumber()) {
            throw new IllegalStateException("Source and target region must be different");
        }

        if (influenceCount < 0) {
            throw new IllegalStateException("Influence count cannot be negative");
        }

        if (influenceCount == 0 && unitIds.isEmpty()) {
            throw new IllegalStateException("Nothing to move");
        }
        // =========================
        // WALIDACJA FROM
        // =========================

        if (from.isClosed()) {
            throw new IllegalStateException("Cannot take form closed region");
        }

        // =========================
        // WALIDACJA TARGET
        // =========================

        if (!to.isActive()) {
            throw new IllegalStateException("Target region is not active");
        }

        if (to.isClosed()) {
            throw new IllegalStateException("Target region is closed");
        }



        // =========================
        // WALIDACJA POPULATION
        // =========================

        if (player.getPopulation() < 2) {
            throw new IllegalStateException("Not enough population");
        }

        // =========================
        // WALIDACJA INFLUENCE
        // =========================

        long ownedInfluence = from.getInfluenceMarkers().stream()
                .filter(marker -> marker.getPlayerId().equals(playerId))
                .count();

        if (ownedInfluence < influenceCount) {
            throw new IllegalStateException("Not enough influence markers in source region");
        }

        // =========================
        // WALIDACJA UNITS
        // =========================

        List<Unit> unitsToMove = new ArrayList<>();

        for (UUID unitId : unitIds) {

            Unit unit = from.getUnits().stream()
                    .filter(u -> u.getId().equals(unitId))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("Unit not found in source region")
                    );

            if (!unit.getOwnerId().equals(playerId)) {
                throw new IllegalStateException("Cannot move чужую unit");
            }

            unitsToMove.add(unit);
        }

        // =========================
        // MOVE INFLUENCE
        // =========================

        int moved = 0;
        List<InfluenceMarker> toRemove = new ArrayList<>();

        for (InfluenceMarker marker : from.getInfluenceMarkers()) {
            if (marker.getPlayerId().equals(playerId) && moved < influenceCount) {
                toRemove.add(marker);
                moved++;
            }
        }

        from.getInfluenceMarkers().removeAll(toRemove);
        to.getInfluenceMarkers().addAll(toRemove);

        // =========================
        // MOVE UNITS
        // =========================

        for (Unit unit : unitsToMove) {
            from.getUnits().remove(unit);
            to.getUnits().add(unit);
            unit.setRegionId(to.getId());
        }

        // =========================
        // COST
        // =========================

        player.spendPopulation(2);

        return ActionResult.finished();
    }

    @Override
    public boolean isFinished(ActionContext context) {
        return true;
    }
}