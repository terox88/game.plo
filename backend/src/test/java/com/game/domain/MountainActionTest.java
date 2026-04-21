package com.game.domain;
import com.game.game.domain.*;
import com.game.game.domain.action.*;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
public class MountainActionTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    @Test
    void shouldMoveInfluenceAndUnitWithMountain() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        from.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        Unit unit = new Unit(playerId, 1, from.getId());
        from.getUnits().add(unit);

        int beforePopulation = player.getPopulation();

        MountainAction action = new MountainAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOUNTAIN)
        );

        action.start(context);

        action.handleDecision(
                context,
                new PlayerDecision(
                        new MountainDecision(
                                1,
                                2,
                                1,
                                List.of(unit.getId())
                        )
                )
        );

        assertThat(from.getInfluenceMarkers())
                .noneMatch(m -> m.getPlayerId().equals(playerId));

        assertThat(to.getInfluenceMarkers())
                .anyMatch(m -> m.getPlayerId().equals(playerId));

        assertThat(from.getUnits()).doesNotContain(unit);
        assertThat(to.getUnits()).contains(unit);

        assertThat(unit.getRegionId()).isEqualTo(to.getId());

        assertThat(player.getPopulation())
                .isEqualTo(beforePopulation - 2);
    }

    @Test
    void shouldMoveOnlyInfluence() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        from.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MountainAction action = new MountainAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOUNTAIN)
        );

        action.start(context);

        action.handleDecision(
                context,
                new PlayerDecision(
                        new MountainDecision(
                                1,
                                2,
                                1,
                                List.of()
                        )
                )
        );

        assertThat(to.getInfluenceMarkers())
                .anyMatch(m -> m.getPlayerId().equals(playerId));
    }

    @Test
    void shouldMoveOnlyUnits() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 1, from.getId());
        from.getUnits().add(unit);

        MountainAction action = new MountainAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOUNTAIN)
        );

        action.start(context);

        action.handleDecision(
                context,
                new PlayerDecision(
                        new MountainDecision(
                                1,
                                2,
                                0,
                                List.of(unit.getId())
                        )
                )
        );

        assertThat(to.getUnits()).contains(unit);
    }

    @Test
    void shouldThrowWhenTryingToMoveNothing() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        MountainAction action = new MountainAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOUNTAIN)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(
                                new MountainDecision(
                                        1,
                                        2,
                                        0,
                                        List.of()
                                )
                        )
                )
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenTargetRegionClosed() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.CLOSED);

        from.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MountainAction action = new MountainAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOUNTAIN)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(
                                new MountainDecision(
                                        1,
                                        2,
                                        1,
                                        List.of()
                                )
                        )
                )
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenFromRegionClosed() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);
        from.getFeatures().add(RegionFeature.CLOSED);

        from.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MountainAction action = new MountainAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOUNTAIN)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(
                                new MountainDecision(
                                        1,
                                        2,
                                        1,
                                        List.of()
                                )
                        )
                )
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenNotEnoughPopulation() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setPopulation(1);

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        from.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MountainAction action = new MountainAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOUNTAIN)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(
                                new MountainDecision(
                                        1,
                                        2,
                                        1,
                                        List.of()
                                )
                        )
                )
        ).isInstanceOf(IllegalStateException.class);
    }
}
