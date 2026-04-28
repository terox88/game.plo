package com.game.game.domain.service;

import com.game.game.domain.*;

import java.util.UUID;

public class MovementService {

    public void moveUnit(
            GameState game,
            UUID playerId,
            UUID fromRegionId,
            UUID toRegionId,
            UUID unitId,
            boolean requireNeighbor
    ) {
        RegionState from = findRegion(game, fromRegionId);
        RegionState to = findRegion(game, toRegionId);

        validateTargetRegion(to);

        if (requireNeighbor && !from.getNeighbors().contains(to.getNumber())) {
            throw new IllegalStateException("Regions are not neighbors");
        }

        Unit unit = from.getUnits().stream()
                .filter(u -> u.getId().equals(unitId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unit not found"));

        if (!unit.getOwnerId().equals(playerId)) {
            throw new IllegalStateException("Not your unit");
        }

        from.getUnits().remove(unit);
        unit.setRegionId(to.getId());
        to.getUnits().add(unit);
    }

    public void moveInfluence(
            GameState game,
            UUID playerId,
            UUID fromRegionId,
            UUID toRegionId,
            boolean requireNeighbor
    ) {
        RegionState from = findRegion(game, fromRegionId);
        RegionState to = findRegion(game, toRegionId);

        validateTargetRegion(to);

        if (requireNeighbor && !from.getNeighbors().contains(to.getNumber())) {
            throw new IllegalStateException("Regions are not neighbors");
        }

        InfluenceMarker marker = from.getInfluenceMarkers().stream()
                .filter(m -> m.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No influence marker"));

        from.getInfluenceMarkers().remove(marker);
        to.getInfluenceMarkers().add(marker);
    }

    public void validateTargetRegion(RegionState to) {
        if (!to.isActive()) {
            throw new IllegalStateException("Target region not active");
        }

        if (to.isClosed()) {
            throw new IllegalStateException("Target region is closed");
        }
    }

    public RegionState findRegion(GameState game, UUID id) {
        return game.getRegions().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Region not found"));
    }
}