package com.game.game.domain.unitactivation;

import com.game.game.domain.*;

import java.util.*;

public class AttackResolver {

    private static final int ATTACK_COST = 2;

    public boolean hasAnyAttackUnit(GameState game, UUID playerId) {
        PlayerState player = game.findPlayer(playerId);

        return game.getRegions().stream()
                .flatMap(region -> region.getUnits().stream())
                .anyMatch(unit ->
                        unit.getOwnerId().equals(playerId)
                                && player.hasAbility(unit, AbilitiesType.ATTACK)
                );
    }

    public void resolve(GameState game, AttackDecision decision) {
        UUID attackerId = decision.playerId();
        PlayerState attacker = game.findPlayer(attackerId);

        if (!hasAnyAttackUnit(game, attackerId)) {
            throw new IllegalStateException("Player has no units with ATTACK");
        }

        attacker.spendGold(ATTACK_COST);

        if (decision.regionAttacks() == null || decision.regionAttacks().isEmpty()) {
            return;
        }

        for (RegionAttackDecision regionAttack : decision.regionAttacks()) {
            resolveRegionAttack(game, attacker, regionAttack);
        }
    }

    private void resolveRegionAttack(
            GameState game,
            PlayerState attacker,
            RegionAttackDecision regionAttack
    ) {
        RegionState region = findRegion(game, regionAttack.regionId());

        int availableDamage = calculateAvailableDamageInRegion(
                game,
                attacker.getPlayerId(),
                region.getId()
        );

        int assignedDamage = regionAttack.damageAssignments().stream()
                .mapToInt(DamageAssignment::damage)
                .sum();

        if (assignedDamage > availableDamage) {
            throw new IllegalStateException("Assigned damage exceeds available damage");
        }

        for (DamageAssignment assignment : regionAttack.damageAssignments()) {
            if (assignment.damage() <= 0) {
                throw new IllegalStateException("Damage must be positive");
            }

            applyDamage(game, attacker, region, assignment);
        }

        markAttackersAsUsed(attacker, region);
    }

    public int calculateAvailableDamageInRegion(
            GameState game,
            UUID playerId,
            UUID regionId
    ) {
        PlayerState player = game.findPlayer(playerId);
        RegionState region = findRegion(game, regionId);

        return region.getUnits().stream()
                .filter(unit -> unit.getOwnerId().equals(playerId))
                .filter(unit -> player.hasAbility(unit, AbilitiesType.ATTACK))
                .filter(unit -> !unit.isHasAttacked())
                .mapToInt(unit -> player.countAbility(unit, AbilitiesType.ATTACK))
                .sum();
    }

    private void applyDamage(
            GameState game,
            PlayerState attacker,
            RegionState region,
            DamageAssignment assignment
    ) {
        switch (assignment.targetType()) {
            case UNIT -> applyDamageToUnit(game, attacker, region, assignment);
            case INFLUENCE_MARKER -> applyDamageToInfluence(game, attacker, region, assignment);
            case INDEPENDENT_NATION -> applyDamageToIndependentNation(attacker, region, assignment);
        }
    }

    private void applyDamageToUnit(
            GameState game,
            PlayerState attacker,
            RegionState region,
            DamageAssignment assignment
    ) {
        Unit target = region.getUnits().stream()
                .filter(unit -> unit.getId().equals(assignment.targetId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Target unit not found"));

        if (target.getOwnerId().equals(attacker.getPlayerId())) {
            throw new IllegalStateException("Cannot attack own unit");
        }

        if (target.isKilled()) {
            throw new IllegalStateException("Unit is already killed");
        }

        PlayerState defender = game.findPlayer(target.getOwnerId());

        int requiredDamage = calculateUnitDefense(defender, target);

        if (assignment.damage() >= requiredDamage) {
            target.kill();
            attacker.setVictoryPoints(
                    attacker.getVictoryPoints() + target.getLevel()
            );
        }
    }

    private void applyDamageToInfluence(
            GameState game,
            PlayerState attacker,
            RegionState region,
            DamageAssignment assignment
    ) {
        UUID influenceOwnerId = assignment.targetId();

        if (influenceOwnerId.equals(attacker.getPlayerId())) {
            throw new IllegalStateException("Cannot attack own influence marker");
        }

        InfluenceMarker marker = region.getInfluenceMarkers().stream()
                .filter(m -> m.getPlayerId().equals(influenceOwnerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Influence marker not found"));

        if (assignment.damage() < 1) {
            return;
        }

        region.getInfluenceMarkers().remove(marker);

        PlayerState owner = game.findPlayer(influenceOwnerId);
        owner.retrieveInfluenceMarker();

        attacker.setVictoryPoints(
                attacker.getVictoryPoints() + 1
        );
    }

    private void applyDamageToIndependentNation(
            PlayerState attacker,
            RegionState region,
            DamageAssignment assignment
    ) {
        if (region.getNeutralMarkerCount() <= 0) {
            throw new IllegalStateException("No independent nation marker in region");
        }

        if (assignment.damage() < 1) {
            return;
        }

        region.setNeutralMarkerCount(region.getNeutralMarkerCount() - 1);

        attacker.setVictoryPoints(
                attacker.getVictoryPoints() + 1
        );
    }

    public int calculateUnitDefense(PlayerState owner, Unit unit) {
        return unit.getLevel() + owner.countAbility(unit, AbilitiesType.SHIELD);
    }

    public void cleanupKilledUnits(GameState game) {
        for (RegionState region : game.getRegions()) {
            List<Unit> killedUnits = region.getUnits().stream()
                    .filter(Unit::isKilled)
                    .toList();

            for (Unit unit : killedUnits) {
                region.getUnits().remove(unit);
                unit.setRegionId(null);
                unit.resetAfterUnitActivation();

                PlayerState owner = game.findPlayer(unit.getOwnerId());
                returnUnitToReserve(owner, unit);
            }

            region.getUnits().forEach(Unit::resetAfterUnitActivation);
        }
    }

    private void returnUnitToReserve(PlayerState owner, Unit unit) {
        switch (unit.getLevel()) {
            case 1 -> owner.setUnitLevel1(owner.getUnitLevel1() + 1);
            case 2 -> owner.setUnitLevel2(owner.getUnitLevel2() + 1);
            case 3 -> owner.setUnitLevel3(owner.getUnitLevel3() + 1);
            default -> throw new IllegalStateException("Invalid unit level: " + unit.getLevel());
        }
    }

    private void markAttackersAsUsed(PlayerState attacker, RegionState region) {
        region.getUnits().stream()
                .filter(unit -> unit.getOwnerId().equals(attacker.getPlayerId()))
                .filter(unit -> attacker.hasAbility(unit, AbilitiesType.ATTACK))
                .forEach(Unit::markAttacked);
    }

    private RegionState findRegion(GameState game, UUID regionId) {
        return game.getRegions().stream()
                .filter(region -> region.getId().equals(regionId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Region not found"));
    }
}
