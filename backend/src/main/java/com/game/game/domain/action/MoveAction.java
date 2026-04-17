package com.game.game.domain.action;

import com.game.game.domain.*;

import java.util.List;
import java.util.UUID;

public class MoveAction implements GameActionDomain {

    @Override
    public ActionResult start(ActionContext context) {
        return ActionResult.decision(List.of("MOVE"));
    }

    @Override
    public ActionResult handleDecision(ActionContext context, PlayerDecision decision) {

        if (decision.getValue() == MakingChoice.PASS) {
            return ActionResult.finished();
        }

        MoveDecision moveDecision = (MoveDecision) decision.getValue();

        GameState game = context.getGame();
        UUID playerId = context.getMarker().getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        List<SingleMove> moves = moveDecision.moves();

        // =========================
        // WALIDACJA ILOŚCI RUCHÓW
        // =========================

        if (moves.isEmpty() || moves.size() > 2) {
            throw new IllegalStateException("Invalid number of moves");
        }

        // =========================
        // KOSZT WALIDACJA
        // =========================

        if (player.getGold() < moves.size()) {
            throw new IllegalStateException("Not enough gold");
        }

        // =========================
        // WYKONANIE RUCHÓW (SEKWENCYJNIE!)
        // =========================

        for (SingleMove move : moves) {

            RegionState from = findRegion(game, move.fromRegionId());
            RegionState to = findRegion(game, move.toRegionId());

            // -------------------------
            // WALIDACJA REGIONÓW
            // -------------------------

            if (!to.isActive()) {
                throw new IllegalStateException("Target region not active");
            }

            if (to.isClosed()) {
                throw new IllegalStateException("Target region is closed");
            }

            if (!from.getNeighbors().contains(to.getNumber())) {
                throw new IllegalStateException("Regions are not neighbors");
            }

            boolean isUnitMove = move.unitId() != null;
            boolean isInfluenceMove = move.influenceOwnerId() != null;

            if (isUnitMove == isInfluenceMove) {
                throw new IllegalStateException("Must move either unit or influence");
            }

            // =========================
            // UNIT MOVE
            // =========================

            if (isUnitMove) {

                Unit unit = from.getUnits().stream()
                        .filter(u -> u.getId().equals(move.unitId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Unit not found"));

                if (!unit.getOwnerId().equals(playerId)) {
                    throw new IllegalStateException("Not your unit");
                }

                // wykonanie
                from.getUnits().remove(unit);
                unit.setRegionId(to.getId());
                to.getUnits().add(unit);
            }

            // =========================
            // INFLUENCE MOVE
            // =========================

            if (isInfluenceMove) {

                InfluenceMarker marker = from.getInfluenceMarkers().stream()
                        .filter(m -> m.getPlayerId().equals(playerId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No influence marker"));

                // wykonanie
                from.getInfluenceMarkers().remove(marker);
                to.getInfluenceMarkers().add(marker);
            }
        }
        player.spendGold(moves.size());

        return ActionResult.finished();
    }

    @Override
    public boolean isFinished(ActionContext context) {
        return true;
    }

    private RegionState findRegion(GameState game, UUID id) {
        return game.getRegions().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Region not found"));
    }
}