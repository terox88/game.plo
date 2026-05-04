package com.game.domain.unitactivation;

import com.game.game.domain.*;
import com.game.game.domain.unitactivation.*;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class AttackResolverTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    private final AttackResolver resolver = new AttackResolver();

    @Test
    void shouldPayTwoGoldForAttack() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        PlayerState attacker = game.findPlayer(attackerId);

        attacker.addGold(2);
        int goldBefore = attacker.getGold();

        attacker.getLevel1().addAbility(AbilitiesType.ATTACK);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit attackerUnit = new Unit(attackerId, 1, region.getId());
        region.getUnits().add(attackerUnit);

        resolver.resolve(game, new AttackDecision(attackerId, List.of()));

        assertThat(attacker.getGold()).isEqualTo(goldBefore - 2);
    }

    @Test
    void shouldThrowWhenNotEnoughGold() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        PlayerState attacker = game.findPlayer(attackerId);

        attacker.spendGold(1);
        attacker.getLevel1().addAbility(AbilitiesType.ATTACK);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit attackerUnit = new Unit(attackerId, 1, region.getId());
        region.getUnits().add(attackerUnit);

        assertThatThrownBy(() ->
                resolver.resolve(game, new AttackDecision(attackerId, List.of()))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldCalculateOneDamagePerAttackAbility() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        PlayerState attacker = game.findPlayer(attackerId);

        attacker.getLevel3().addAbility(AbilitiesType.ATTACK);
        attacker.getLevel3().addAbility(AbilitiesType.ATTACK);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit attackerUnit = new Unit(attackerId, 3, region.getId());
        region.getUnits().add(attackerUnit);

        int damage = resolver.calculateAvailableDamageInRegion(
                game,
                attackerId,
                region.getId()
        );

        assertThat(damage).isEqualTo(3);
    }

    @Test
    void shouldKillLevelOneUnitWithOneDamage() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        UUID defenderId = players.get(1).getPlayerId();

        PlayerState attacker = game.findPlayer(attackerId);
        attacker.addGold(2);
        attacker.getLevel1().addAbility(AbilitiesType.ATTACK);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit attackerUnit = new Unit(attackerId, 1, region.getId());
        Unit defenderUnit = new Unit(defenderId, 1, region.getId());

        region.getUnits().add(attackerUnit);
        region.getUnits().add(defenderUnit);

        resolver.resolve(game, new AttackDecision(
                attackerId,
                List.of(new RegionAttackDecision(
                        region.getId(),
                        List.of(new DamageAssignment(
                                AttackTargetType.UNIT,
                                defenderUnit.getId(),
                                1
                        ))
                ))
        ));

        assertThat(defenderUnit.isKilled()).isTrue();
        assertThat(region.getUnits()).contains(defenderUnit);
        assertThat(attacker.getVictoryPoints()).isEqualTo(1);
    }

    @Test
    void shouldNotKillShieldedLevelOneUnitWithOneDamage() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        UUID defenderId = players.get(1).getPlayerId();

        PlayerState attacker = game.findPlayer(attackerId);
        PlayerState defender = game.findPlayer(defenderId);

        attacker.addGold(2);
        attacker.getLevel1().addAbility(AbilitiesType.ATTACK);

        defender.getLevel1().addAbility(AbilitiesType.SHIELD);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit attackerUnit = new Unit(attackerId, 1, region.getId());
        Unit defenderUnit = new Unit(defenderId, 1, region.getId());

        region.getUnits().add(attackerUnit);
        region.getUnits().add(defenderUnit);

        resolver.resolve(game, new AttackDecision(
                attackerId,
                List.of(new RegionAttackDecision(
                        region.getId(),
                        List.of(new DamageAssignment(
                                AttackTargetType.UNIT,
                                defenderUnit.getId(),
                                1
                        ))
                ))
        ));

        assertThat(defenderUnit.isKilled()).isFalse();
        assertThat(attacker.getVictoryPoints()).isEqualTo(0);
    }

    @Test
    void shouldKillShieldedLevelOneUnitWithTwoDamage() {
        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        UUID defenderId = players.get(1).getPlayerId();

        PlayerState attacker = game.findPlayer(attackerId);
        PlayerState defender = game.findPlayer(defenderId);

        attacker.addGold(2);

        defender.getLevel1().addAbility(AbilitiesType.SHIELD);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit attackerUnit = new Unit(attackerId, 1, region.getId());
        Unit defenderUnit = new Unit(defenderId, 1, region.getId());

        region.getUnits().add(attackerUnit);
        region.getUnits().add(defenderUnit);

        resolver.resolve(game, new AttackDecision(
                attackerId,
                List.of(new RegionAttackDecision(
                        region.getId(),
                        List.of(new DamageAssignment(
                                AttackTargetType.UNIT,
                                defenderUnit.getId(),
                                2
                        ))
                ))
        ));

        assertThat(defenderUnit.isKilled()).isTrue();
        assertThat(attacker.getVictoryPoints()).isEqualTo(1);
    }

    @Test
    void shouldKillInfluenceMarkerAndReturnItToOwnerPool() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        UUID defenderId = players.get(1).getPlayerId();

        PlayerState attacker = game.findPlayer(attackerId);
        PlayerState defender = game.findPlayer(defenderId);

        attacker.addGold(2);
        attacker.getLevel1().addAbility(AbilitiesType.ATTACK);
        defender.setAvailableInfluenceMarkers(18);

        int defenderAvailableBefore = defender.getAvailableInfluenceMarkers();

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getInfluenceMarkers().add(new InfluenceMarker(defenderId));

        Unit attackerUnit = new Unit(attackerId, 1, region.getId());
        region.getUnits().add(attackerUnit);

        resolver.resolve(game, new AttackDecision(
                attackerId,
                List.of(new RegionAttackDecision(
                        region.getId(),
                        List.of(new DamageAssignment(
                                AttackTargetType.INFLUENCE_MARKER,
                                defenderId,
                                1
                        ))
                ))
        ));

        assertThat(region.getInfluenceMarkers())
                .noneMatch(marker -> marker.getPlayerId().equals(defenderId));

        assertThat(defender.getAvailableInfluenceMarkers())
                .isEqualTo(defenderAvailableBefore + 1);

        assertThat(attacker.getVictoryPoints()).isEqualTo(1);
    }

    @Test
    void shouldKillIndependentNationMarker() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();

        PlayerState attacker = game.findPlayer(attackerId);
        attacker.addGold(2);
        attacker.getLevel1().addAbility(AbilitiesType.ATTACK);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.setNeutralMarkerCount(1);

        Unit attackerUnit = new Unit(attackerId, 1, region.getId());
        region.getUnits().add(attackerUnit);

        resolver.resolve(game, new AttackDecision(
                attackerId,
                List.of(new RegionAttackDecision(
                        region.getId(),
                        List.of(new DamageAssignment(
                                AttackTargetType.INDEPENDENT_NATION,
                                null,
                                1
                        ))
                ))
        ));

        assertThat(region.getNeutralMarkerCount()).isEqualTo(0);
        assertThat(attacker.getVictoryPoints()).isEqualTo(1);
    }

    @Test
    void shouldNotAllowAttackingOwnUnit() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();

        PlayerState attacker = game.findPlayer(attackerId);
        attacker.addGold(2);
        attacker.getLevel1().addAbility(AbilitiesType.ATTACK);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit attackerUnit = new Unit(attackerId, 1, region.getId());
        Unit ownTarget = new Unit(attackerId, 1, region.getId());

        region.getUnits().add(attackerUnit);
        region.getUnits().add(ownTarget);

        assertThatThrownBy(() ->
                resolver.resolve(game, new AttackDecision(
                        attackerId,
                        List.of(new RegionAttackDecision(
                                region.getId(),
                                List.of(new DamageAssignment(
                                        AttackTargetType.UNIT,
                                        ownTarget.getId(),
                                        1
                                ))
                        ))
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldNotAllowAssignedDamageExceedingAvailableDamage() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        UUID defenderId = players.get(1).getPlayerId();

        PlayerState attacker = game.findPlayer(attackerId);
        attacker.addGold(2);
        attacker.getLevel1().addAbility(AbilitiesType.ATTACK);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit attackerUnit = new Unit(attackerId, 1, region.getId());
        Unit defenderUnit = new Unit(defenderId, 1, region.getId());

        region.getUnits().add(attackerUnit);
        region.getUnits().add(defenderUnit);

        assertThatThrownBy(() ->
                resolver.resolve(game, new AttackDecision(
                        attackerId,
                        List.of(new RegionAttackDecision(
                                region.getId(),
                                List.of(new DamageAssignment(
                                        AttackTargetType.UNIT,
                                        defenderUnit.getId(),
                                        2
                                ))
                        ))
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldCleanupKilledUnitsAndReturnThemToReserve() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID defenderId = players.get(1).getPlayerId();
        PlayerState defender = game.findPlayer(defenderId);

        int reserveBefore = defender.getUnitLevel1();

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit killedUnit = new Unit(defenderId, 1, region.getId());
        killedUnit.kill();
        region.getUnits().add(killedUnit);

        resolver.cleanupKilledUnits(game);

        assertThat(region.getUnits()).doesNotContain(killedUnit);
        assertThat(killedUnit.getRegionId()).isNull();
        assertThat(killedUnit.isKilled()).isFalse();
        assertThat(defender.getUnitLevel1()).isEqualTo(reserveBefore + 1);
    }

    @Test
    void shouldAllowKilledUnitToAttackBeforeCleanup() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        UUID defenderId = players.get(1).getPlayerId();

        PlayerState attacker = game.findPlayer(attackerId);
        attacker.addGold(2);
        attacker.getLevel1().addAbility(AbilitiesType.ATTACK);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit killedButStillAttacking = new Unit(attackerId, 1, region.getId());
        killedButStillAttacking.kill();

        Unit defenderUnit = new Unit(defenderId, 1, region.getId());

        region.getUnits().add(killedButStillAttacking);
        region.getUnits().add(defenderUnit);

        resolver.resolve(game, new AttackDecision(
                attackerId,
                List.of(new RegionAttackDecision(
                        region.getId(),
                        List.of(new DamageAssignment(
                                AttackTargetType.UNIT,
                                defenderUnit.getId(),
                                1
                        ))
                ))
        ));

        assertThat(defenderUnit.isKilled()).isTrue();
    }
}