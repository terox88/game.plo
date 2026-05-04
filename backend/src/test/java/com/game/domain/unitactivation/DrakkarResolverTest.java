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

class DrakkarResolverTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    private final DrakkarResolver resolver = new DrakkarResolver();

    @Test
    void shouldMoveUnitBetweenDrakkarRegions() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(3);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit drakkarA = new Unit(playerId, 2, from.getId());
        Unit drakkarB = new Unit(playerId, 2, to.getId());
        Unit movedUnit = new Unit(playerId, 1, from.getId());

        from.getUnits().add(drakkarA);
        to.getUnits().add(drakkarB);
        from.getUnits().add(movedUnit);

        resolver.resolve(game, new DrakkarDecision(
                playerId,
                from.getId(),
                to.getId(),
                List.of(movedUnit.getId()),
                0
        ));

        assertThat(from.getUnits()).doesNotContain(movedUnit);
        assertThat(to.getUnits()).contains(movedUnit);
        assertThat(movedUnit.getRegionId()).isEqualTo(to.getId());
    }

    @Test
    void shouldMoveInfluenceBetweenDrakkarRegions() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);


        player.useInfluenceMarker();

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(3);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit drakkarA = new Unit(playerId, 2, from.getId());
        Unit drakkarB = new Unit(playerId, 2, to.getId());

        from.getUnits().add(drakkarA);
        to.getUnits().add(drakkarB);

        from.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        resolver.resolve(game, new DrakkarDecision(
                playerId,
                from.getId(),
                to.getId(),
                List.of(),
                1
        ));

        assertThat(from.getInfluenceMarkers()).isEmpty();

        assertThat(to.getInfluenceMarkers())
                .anyMatch(marker -> marker.getPlayerId().equals(playerId));
    }

    @Test
    void shouldNotMoveDrakkarUnitItself() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);


        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(3);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit drakkarA = new Unit(playerId, 2, from.getId());
        Unit drakkarB = new Unit(playerId, 2, to.getId());

        from.getUnits().add(drakkarA);
        to.getUnits().add(drakkarB);

        assertThatThrownBy(() ->
                resolver.resolve(game, new DrakkarDecision(
                        playerId,
                        from.getId(),
                        to.getId(),
                        List.of(drakkarA.getId()),
                        0
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRequireBothDrakkarsOnBoard() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);


        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(3);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit onlyOneDrakkar = new Unit(playerId, 2, from.getId());
        Unit movedUnit = new Unit(playerId, 1, from.getId());

        from.getUnits().add(onlyOneDrakkar);
        from.getUnits().add(movedUnit);

        assertThat(resolver.canResolve(game, playerId)).isFalse();

        assertThatThrownBy(() ->
                resolver.resolve(game, new DrakkarDecision(
                        playerId,
                        from.getId(),
                        to.getId(),
                        List.of(movedUnit.getId()),
                        0
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRequireDrakkarsInDifferentRegions() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);


        RegionState region = game.getRegionByNumber(1);

        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit drakkarA = new Unit(playerId, 2, region.getId());
        Unit drakkarB = new Unit(playerId, 2, region.getId());

        region.getUnits().add(drakkarA);
        region.getUnits().add(drakkarB);

        assertThat(resolver.canResolve(game, playerId)).isFalse();
    }

    @Test
    void shouldRequireMovementBetweenDrakkarRegions() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);


        RegionState drakkarRegionA = game.getRegionByNumber(1);
        RegionState drakkarRegionB = game.getRegionByNumber(3);
        RegionState invalidRegion = game.getRegionByNumber(4);

        drakkarRegionA.getFeatures().add(RegionFeature.IN_GAME);
        drakkarRegionB.getFeatures().add(RegionFeature.IN_GAME);
        invalidRegion.getFeatures().add(RegionFeature.IN_GAME);

        Unit drakkarA = new Unit(playerId, 2, drakkarRegionA.getId());
        Unit drakkarB = new Unit(playerId, 2, drakkarRegionB.getId());
        Unit movedUnit = new Unit(playerId, 1, invalidRegion.getId());

        drakkarRegionA.getUnits().add(drakkarA);
        drakkarRegionB.getUnits().add(drakkarB);
        invalidRegion.getUnits().add(movedUnit);

        assertThatThrownBy(() ->
                resolver.resolve(game, new DrakkarDecision(
                        playerId,
                        invalidRegion.getId(),
                        drakkarRegionA.getId(),
                        List.of(movedUnit.getId()),
                        0
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldNotMoveKilledUnit() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);


        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(3);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit drakkarA = new Unit(playerId, 2, from.getId());
        Unit drakkarB = new Unit(playerId, 2, to.getId());
        Unit killedUnit = new Unit(playerId, 1, from.getId());
        killedUnit.kill();

        from.getUnits().add(drakkarA);
        to.getUnits().add(drakkarB);
        from.getUnits().add(killedUnit);

        assertThatThrownBy(() ->
                resolver.resolve(game, new DrakkarDecision(
                        playerId,
                        from.getId(),
                        to.getId(),
                        List.of(killedUnit.getId()),
                        0
                ))
        ).isInstanceOf(IllegalStateException.class);
    }
}