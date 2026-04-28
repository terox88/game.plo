package com.game.game.domain.unitactivation;

import com.game.game.domain.*;
import com.game.game.domain.service.MovementService;

import java.util.List;
import java.util.UUID;

public class DrakkarResolver {

    private final MovementService movementService = new MovementService();

    public boolean canResolve(GameState game, UUID playerId) {
        List<UnitLocation> drakkars = findDrakkarsOnBoard(game, playerId);

        if (drakkars.size() != 2) {
            return false;
        }

        UUID firstRegionId = drakkars.get(0).region().getId();
        UUID secondRegionId = drakkars.get(1).region().getId();

        return !firstRegionId.equals(secondRegionId);
    }

    public void resolve(GameState game, DrakkarDecision decision) {
        UUID playerId = decision.playerId();

        if (!canResolve(game, playerId)) {
            throw new IllegalStateException("Drakkar cannot be resolved");
        }

        validateRegions(game, decision);

        if (decision.unitIds() != null) {
            for (UUID unitId : decision.unitIds()) {
                validateMovedUnitIsNotDrakkar(game, playerId, decision.fromRegionId(), unitId);

                movementService.moveUnit(
                        game,
                        playerId,
                        decision.fromRegionId(),
                        decision.toRegionId(),
                        unitId,
                        false
                );
            }
        }

        if (decision.influenceMarkersAmount() < 0) {
            throw new IllegalStateException("Invalid influence markers amount");
        }

        for (int i = 0; i < decision.influenceMarkersAmount(); i++) {
            movementService.moveInfluence(
                    game,
                    playerId,
                    decision.fromRegionId(),
                    decision.toRegionId(),
                    false
            );
        }
    }

    private void validateRegions(GameState game, DrakkarDecision decision) {
        List<UnitLocation> drakkars = findDrakkarsOnBoard(game, decision.playerId());

        UUID firstRegionId = drakkars.get(0).region().getId();
        UUID secondRegionId = drakkars.get(1).region().getId();

        boolean fromIsDrakkarRegion =
                decision.fromRegionId().equals(firstRegionId)
                        || decision.fromRegionId().equals(secondRegionId);

        boolean toIsDrakkarRegion =
                decision.toRegionId().equals(firstRegionId)
                        || decision.toRegionId().equals(secondRegionId);

        if (!fromIsDrakkarRegion || !toIsDrakkarRegion) {
            throw new IllegalStateException("Movement must be between Drakkar regions");
        }

        if (decision.fromRegionId().equals(decision.toRegionId())) {
            throw new IllegalStateException("Cannot move within the same region");
        }
    }

    private void validateMovedUnitIsNotDrakkar(
            GameState game,
            UUID playerId,
            UUID fromRegionId,
            UUID unitId
    ) {
        PlayerState player = game.findPlayer(playerId);
        RegionState from = movementService.findRegion(game, fromRegionId);

        Unit unit = from.getUnits().stream()
                .filter(u -> u.getId().equals(unitId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unit not found"));

        if (!unit.getOwnerId().equals(playerId)) {
            throw new IllegalStateException("Not your unit");
        }

        if (player.hasAbility(unit, AbilitiesType.DRAKAR)) {
            throw new IllegalStateException("Drakkar unit cannot be moved by Drakkar ability");
        }

        if (unit.isKilled()) {
            throw new IllegalStateException("Killed unit cannot be moved");
        }
    }

    private List<UnitLocation> findDrakkarsOnBoard(GameState game, UUID playerId) {
        PlayerState player = game.findPlayer(playerId);

        return game.getRegions().stream()
                .flatMap(region -> region.getUnits().stream()
                        .filter(unit -> unit.getOwnerId().equals(playerId))
                        .filter(unit -> player.hasAbility(unit, AbilitiesType.DRAKAR))
                        .filter(unit -> !unit.isKilled())
                        .map(unit -> new UnitLocation(unit, region))
                )
                .toList();
    }

    private record UnitLocation(
            Unit unit,
            RegionState region
    ) {}
}