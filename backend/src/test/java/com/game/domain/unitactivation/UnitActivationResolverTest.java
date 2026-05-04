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

class UnitActivationResolverTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    private final UnitActivationResolver resolver = new UnitActivationResolver();

    @Test
    void shouldStartUnitActivationWithSnapshotTurnOrder() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerA = players.get(0).getPlayerId();
        UUID playerB = players.get(1).getPlayerId();

        game.setInitiativeTurnOrder(List.of(playerB, playerA));

        addSpeedUnit(game, playerB, 1);

        resolver.start(game);

        assertThat(game.getCurrentPhase()).isEqualTo(Phase.UNIT_ACTIVATION);
        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.SPEED);
        assertThat(game.getCurrentTurnOrder()).containsExactly(playerB, playerA);
        assertThat(game.getCurrentPlayerId()).isEqualTo(playerB);
    }

    @Test
    void shouldSkipUnavailableStepsAndMoveToSpeed() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerA = players.get(0).getPlayerId();
        UUID playerB = players.get(1).getPlayerId();

        game.setInitiativeTurnOrder(List.of(playerA, playerB));

        addSpeedUnit(game, playerA, 1);

        resolver.start(game);

        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.SPEED);
        assertThat(game.getCurrentPlayerId()).isEqualTo(playerA);
    }

    @Test
    void shouldMoveFromSpeedPlayerToNextSpeedPlayer() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerA = players.get(0).getPlayerId();
        UUID playerB = players.get(1).getPlayerId();

        game.setInitiativeTurnOrder(List.of(playerA, playerB));

        Unit unitA = addSpeedUnit(game, playerA, 1);
        Unit unitB = addSpeedUnit(game, playerB, 3);

        RegionState r1 = game.getRegionByNumber(1);
        r1.getFeatures().add(RegionFeature.IN_GAME);
        RegionState r2 = game.getRegionByNumber(2);
        r2.getFeatures().add(RegionFeature.IN_GAME);

        resolver.start(game);

        resolver.handleSpeedDecision(game, new SpeedDecision(
                playerA,
                List.of(new SpeedMove(r1.getId(), r2.getId(), unitA.getId()))
        ));

        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.SPEED);
        assertThat(game.getCurrentPlayerId()).isEqualTo(playerB);

        RegionState r3 = game.getRegionByNumber(3);
        RegionState r4 = game.getRegionByNumber(6);
        r3.getFeatures().add(RegionFeature.IN_GAME);
        r4.getFeatures().add(RegionFeature.IN_GAME);

        resolver.handleSpeedDecision(game, new SpeedDecision(
                playerB,
                List.of(new SpeedMove(r3.getId(), r4.getId(), unitB.getId()))
        ));

        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.ATTACK);
    }

    @Test
    void shouldSkipSpeedAndMoveToAttack() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerA = players.get(0).getPlayerId();
        UUID playerB = players.get(1).getPlayerId();

        game.setInitiativeTurnOrder(List.of(playerA, playerB));

        addSpeedUnitLevel2(game, playerA, 1);
        addAttackUnit(game, playerB, 1);

        game.findPlayer(playerB).addGold(2);

        resolver.start(game);

        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.SPEED);
        assertThat(game.getCurrentPlayerId()).isEqualTo(playerA);

        resolver.skipSpeed(game, playerA);

        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.ATTACK);
        assertThat(game.getCurrentPlayerId()).isEqualTo(playerB);
    }

    @Test
    void shouldResolveAttackThenFinishToVuko() {
        var players = List.of(
                playerFactory.create("A", Hero.PASSIONARIA),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID attackerId = players.get(0).getPlayerId();
        UUID defenderId = players.get(1).getPlayerId();

        game.setInitiativeTurnOrder(List.of(attackerId));

        PlayerState attacker = game.findPlayer(attackerId);
        attacker.addGold(2);

        if (!attacker.getLevel2().hasAbility(AbilitiesType.ATTACK)) {
            attacker.getLevel2().addAbility(AbilitiesType.ATTACK);
        }

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit attackerUnit = new Unit(attackerId, 2, region.getId());
        Unit defenderUnit = new Unit(defenderId, 1, region.getId());

        region.getUnits().add(attackerUnit);
        region.getUnits().add(defenderUnit);

        resolver.start(game);

        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.ATTACK);
        assertThat(game.getCurrentPlayerId()).isEqualTo(attackerId);

        resolver.handleAttackDecision(game, new AttackDecision(
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

        assertThat(game.getCurrentPhase()).isEqualTo(Phase.VUKO);
        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.END);
        assertThat(game.getCurrentPlayerId()).isNull();

        assertThat(region.getUnits()).doesNotContain(defenderUnit);
        assertThat(defenderUnit.getRegionId()).isNull();
        assertThat(attacker.getVictoryPoints()).isEqualTo(1);
    }

    @Test
    void shouldRejectDecisionFromWrongPlayer() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerA = players.get(0).getPlayerId();
        UUID playerB = players.get(1).getPlayerId();

        game.setInitiativeTurnOrder(List.of(playerA, playerB));

        addSpeedUnit(game, playerA, 1);

        resolver.start(game);

        assertThatThrownBy(() ->
                resolver.skipSpeed(game, playerB)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldResolveMakerSpyBeforeSpeed() {
        var players = List.of(
                playerFactory.create("A", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        game.setInitiativeTurnOrder(List.of(playerId));

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getUnits().add(new Unit(playerId, 3, region.getId()));

        resolver.start(game);

        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.MAKER_SPY);
        assertThat(game.getCurrentPlayerId()).isEqualTo(playerId);

        resolver.handleMakerSpyDecision(game, new MakerSpyDecision(
                playerId,
                AbilitiesType.SPEED,
                AbilitiesType.ATTACK
        ));

        assertThat(game.getUnitActivationStep()).isEqualTo(UnitActivationStep.SPEED);
        assertThat(game.getCurrentPlayerId()).isEqualTo(playerId);
    }

    private Unit addSpeedUnit(GameState game, UUID playerId, int regionNumber) {
        PlayerState player = game.findPlayer(playerId);
        player.getLevel1().addAbility(AbilitiesType.SPEED);

        RegionState region = game.getRegionByNumber(regionNumber);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 1, region.getId());
        region.getUnits().add(unit);

        return unit;
    }
    private Unit addSpeedUnitLevel2(GameState game, UUID playerId, int regionNumber) {
        PlayerState player = game.findPlayer(playerId);

        if (!player.getLevel2().hasAbility(AbilitiesType.SPEED)) {
            player.getLevel2().addAbility(AbilitiesType.SPEED);
        }

        RegionState region = game.getRegionByNumber(regionNumber);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 2, region.getId());
        region.getUnits().add(unit);

        return unit;
    }

    private Unit addAttackUnit(GameState game, UUID playerId, int regionNumber) {
        PlayerState player = game.findPlayer(playerId);
        player.getLevel1().addAbility(AbilitiesType.ATTACK);

        RegionState region = game.getRegionByNumber(regionNumber);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 1, region.getId());
        region.getUnits().add(unit);

        return unit;
    }
}