package com.game.game.domain.unitactivation;

import com.game.game.domain.*;
import com.game.game.domain.service.MovementService;

import java.util.UUID;

public class SpeedResolver {

    private final MovementService movementService = new MovementService();

    public boolean hasAnySpeedUnit(GameState game, UUID playerId) {
        PlayerState player = game.findPlayer(playerId);

        return game.getRegions().stream()
                .flatMap(region -> region.getUnits().stream())
                .anyMatch(unit ->
                        unit.getOwnerId().equals(playerId)
                                && player.hasAbility(unit, AbilitiesType.SPEED)
                );
    }

    public void resolve(GameState game, SpeedDecision decision) {
        UUID playerId = decision.playerId();
        PlayerState player = game.findPlayer(playerId);

        if (decision.moves() == null || decision.moves().isEmpty()) {
            return;
        }

        for (SpeedMove move : decision.moves()) {
            validateSpeedMove(game, player, move);

            movementService.moveUnit(
                    game,
                    playerId,
                    move.fromRegionId(),
                    move.toRegionId(),
                    move.unitId(),
                    true
            );
        }
    }

    private void validateSpeedMove(GameState game, PlayerState player, SpeedMove move) {
        RegionState from = movementService.findRegion(game, move.fromRegionId());

        Unit unit = from.getUnits().stream()
                .filter(u -> u.getId().equals(move.unitId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unit not found"));

        if (!unit.getOwnerId().equals(player.getPlayerId())) {
            throw new IllegalStateException("Not your unit");
        }

        if (!player.hasAbility(unit, AbilitiesType.SPEED)) {
            throw new IllegalStateException("Unit does not have SPEED");
        }

        if (unit.isKilled()) {
            throw new IllegalStateException("Killed unit cannot move");
        }
    }
}