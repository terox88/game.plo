package com.game.domain.action;

import com.game.game.domain.*;
import com.game.game.domain.action.*;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
public class MoveActionTest {
    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    @Test
    void shouldMoveUnitToNeighborRegion() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.addGold(2);

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);


        Unit unit = new Unit(playerId, 1, from.getId());
        from.getUnits().add(unit);

        MoveAction action = new MoveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOVE)
        );

        action.start(context);

        action.handleDecision(context, new PlayerDecision(
                new MoveDecision(List.of(
                        new SingleMove(from.getId(), to.getId(), unit.getId(), null)
                ))
        ));

        assertThat(to.getUnits()).contains(unit);
        assertThat(from.getUnits()).doesNotContain(unit);
    }

    @Test
    void shouldMoveInfluence() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.addGold(1);

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        from.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MoveAction action = new MoveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOVE)
        );

        action.start(context);

        action.handleDecision(context, new PlayerDecision(
                new MoveDecision(List.of(
                        new SingleMove(from.getId(), to.getId(), null, playerId)
                ))
        ));

        assertThat(to.getInfluenceMarkers())
                .anyMatch(m -> m.getPlayerId().equals(playerId));

        assertThat(from.getInfluenceMarkers()).isEmpty();
    }

    @Test
    void shouldAllowChainedMove() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.addGold(2);

        RegionState a = game.getRegionByNumber(1);
        RegionState b = game.getRegionByNumber(2);
        RegionState c = game.getRegionByNumber(3);

        a.getFeatures().add(RegionFeature.IN_GAME);
        b.getFeatures().add(RegionFeature.IN_GAME);
        c.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 1, a.getId());
        a.getUnits().add(unit);

        MoveAction action = new MoveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOVE)
        );

        action.start(context);

        action.handleDecision(context, new PlayerDecision(
                new MoveDecision(List.of(
                        new SingleMove(a.getId(), b.getId(), unit.getId(), null),
                        new SingleMove(b.getId(), c.getId(), unit.getId(), null)
                ))
        ));

        assertThat(c.getUnits()).contains(unit);
    }

    @Test
    void shouldThrowWhenNotNeighbors() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.addGold(1);

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(3); // ❗ nie są sąsiadami

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);

        Unit unit = new Unit(playerId, 1, from.getId());
        from.getUnits().add(unit);

        MoveAction action = new MoveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOVE)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(context, new PlayerDecision(
                        new MoveDecision(List.of(
                                new SingleMove(from.getId(), to.getId(), unit.getId(), null)
                        ))
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenRegionClosed() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.addGold(1);

        RegionState from = game.getRegionByNumber(1);
        RegionState to = game.getRegionByNumber(2);

        from.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.IN_GAME);
        to.getFeatures().add(RegionFeature.CLOSED);

        Unit unit = new Unit(playerId, 1, from.getId());
        from.getUnits().add(unit);

        MoveAction action = new MoveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MOVE)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(context, new PlayerDecision(
                        new MoveDecision(List.of(
                                new SingleMove(from.getId(), to.getId(), unit.getId(), null)
                        ))
                ))
        ).isInstanceOf(IllegalStateException.class);
    }
}
