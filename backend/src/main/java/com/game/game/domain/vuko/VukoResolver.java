package com.game.game.domain.vuko;

import com.game.game.domain.*;
import com.game.game.domain.service.ReputationService;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class VukoResolver {

    private final ReputationService reputationService = new ReputationService();

    public void start(GameState game) {
        UUID targetID = reputationService.getWorstPlayer(game);
        PlayerState target = game.findPlayer(targetID);

        RegionState targetRegion = findTargetRegion(game, target);

        moveVuko(game, target, targetRegion);

        target.setVukoTokens(target.getVukoTokens() + 1);
        List<Unit> units = targetRegion.getUnits().stream()
                .filter(u -> u.getOwnerId().equals(target.getPlayerId()))
                .toList();

        if (units.isEmpty()) {
            finish(game);
            return;
        }

        if (units.size() == 1) {
            units.get(0).kill();
            finish(game);
            return;
        }

        game.setVukoStep(VukoStep.WAITING_FOR_KILL);
        game.setCurrentPlayerId(target.getPlayerId());

    }

    public void handleKillDecision(GameState game, VukoKillDecision decision) {
        validateKillDecision(game, decision);

        RegionState region = findRegion(game, game.getVukoRegionId());

        Unit unit = region.getUnits().stream()
                .filter(u -> u.getId().equals(decision.unitId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unit not found"));

        if (!unit.getOwnerId().equals(decision.playerId())) {
            throw new IllegalStateException("Cannot kill unit of another player");
        }

        unit.kill();

        finish(game);
    }

    private RegionState findTargetRegion(GameState game, PlayerState player) {

        List<RegionState> regions = game.getRegions().stream()
                .filter(r -> r.isActive())
                .filter(r -> !r.isClosed())
                .filter(r -> countInfluence(r, player.getPlayerId()) > 0)
                .toList();

        if (regions.isEmpty()) {
            throw new IllegalStateException("Player has no influence on board");
        }

        UUID currentVukoRegion = game.getVukoRegionId();

        return regions.stream()
                .sorted(Comparator
                        .comparingInt((RegionState r) -> countInfluence(r, player.getPlayerId()))
                        .reversed()
                        .thenComparing(r -> r.getLandToken().getOrderNumber())
                )
                .filter(r -> !r.getId().equals(currentVukoRegion)
                        || regions.size() == 1)
                .findFirst()
                .orElseThrow();
    }

    private void moveVuko(GameState game, PlayerState player, RegionState targetRegion) {
        game.setVukoRegionId(targetRegion.getId());
    }

    private boolean hasUnitsInRegion(PlayerState player, RegionState region) {
        return region.getUnits().stream()
                .anyMatch(u -> u.getOwnerId().equals(player.getPlayerId()));
    }

    private int countInfluence(RegionState region, UUID playerId) {
        return (int) region.getInfluenceMarkers().stream()
                .filter(m -> m.getPlayerId().equals(playerId))
                .count();
    }

    private void validateKillDecision(GameState game, VukoKillDecision decision) {
        if (game.getVukoStep() != VukoStep.WAITING_FOR_KILL) {
            throw new IllegalStateException("Not waiting for kill decision");
        }

        if (!decision.playerId().equals(game.getCurrentPlayerId())) {
            throw new IllegalStateException("Not your decision");
        }

    }

    private RegionState findRegion(GameState game, UUID id) {
        return game.getRegions().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private void finish(GameState game) {
        game.setVukoStep(VukoStep.END);
        game.setCurrentPlayerId(null);

        // następna faza
        game.setCurrentPhase(Phase.DOMINATION);
    }
}