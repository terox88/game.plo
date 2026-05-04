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

class SpeedResolverTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    private final SpeedResolver resolver = new SpeedResolver();

    @Test
    void shouldMoveSpeedUnitToNeighborRegion() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.getLevel1().addAbility(AbilitiesType.SPEED);

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 1, from.getId());
        from.getUnits().add(unit);

        resolver.resolve(game, new SpeedDecision(
                playerId,
                List.of(new SpeedMove(from.getId(), to.getId(), unit.getId()))
        ));

        assertThat(to.getUnits()).contains(unit);
        assertThat(from.getUnits()).doesNotContain(unit);
        assertThat(unit.getRegionId()).isEqualTo(to.getId());
    }

    @Test
    void shouldNotMoveUnitWithoutSpeed() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 1, from.getId());
        from.getUnits().add(unit);

        assertThatThrownBy(() ->
                resolver.resolve(game, new SpeedDecision(
                        playerId,
                        List.of(new SpeedMove(from.getId(), to.getId(), unit.getId()))
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldNotMoveToNonNeighborRegion() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.getLevel1().addAbility(AbilitiesType.SPEED);

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(3);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 1, from.getId());
        from.getUnits().add(unit);

        assertThatThrownBy(() ->
                resolver.resolve(game, new SpeedDecision(
                        playerId,
                        List.of(new SpeedMove(from.getId(), to.getId(), unit.getId()))
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldMoveMultipleSpeedUnitsIndependently() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.getLevel1().addAbility(AbilitiesType.SPEED);

        RegionState r1 = game.getRegionByNumber(1);
        RegionState r2 = game.getRegionByNumber(2);
        RegionState r3 = game.getRegionByNumber(3);
        RegionState r4 = game.getRegionByNumber(6);

        r1.getFeatures().add(RegionFeature.IN_GAME);
        r2.getFeatures().add(RegionFeature.IN_GAME);
        r3.getFeatures().add(RegionFeature.IN_GAME);
        r4.getFeatures().add(RegionFeature.IN_GAME);

        Unit unitA = new Unit(playerId, 1, r1.getId());
        Unit unitB = new Unit(playerId, 1, r3.getId());

        r1.getUnits().add(unitA);
        r3.getUnits().add(unitB);

        resolver.resolve(game, new SpeedDecision(
                playerId,
                List.of(
                        new SpeedMove(r1.getId(), r2.getId(), unitA.getId()),
                        new SpeedMove(r3.getId(), r4.getId(), unitB.getId())
                )
        ));

        assertThat(r2.getUnits()).contains(unitA);
        assertThat(r1.getUnits()).doesNotContain(unitA);

        assertThat(r4.getUnits()).contains(unitB);
        assertThat(r3.getUnits()).doesNotContain(unitB);
    }

    @Test
    void shouldNotMoveKilledUnit() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.getLevel1().addAbility(AbilitiesType.SPEED);

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 1, from.getId());
        unit.kill();
        from.getUnits().add(unit);

        assertThatThrownBy(() ->
                resolver.resolve(game, new SpeedDecision(
                        playerId,
                        List.of(new SpeedMove(from.getId(), to.getId(), unit.getId()))
                ))
        ).isInstanceOf(IllegalStateException.class);
    }
}