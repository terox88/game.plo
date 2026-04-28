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
public class InfluenceActionTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    @Test
    void shouldPlaceOneInfluenceMarker() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);


        region.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        int beforePopulation = player.getPopulation();
        int beforeMarkers = region.getInfluenceMarkers().size();

        InfluenceAction action = new InfluenceAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.INFLUENCES)
        );

        action.start(context);

        action.handleDecision(
                context,
                new PlayerDecision(
                        new InfluenceDecision(List.of(1))
                )
        );

        assertThat(region.getInfluenceMarkers().size())
                .isEqualTo(beforeMarkers + 1);

        assertThat(player.getPopulation())
                .isEqualTo(beforePopulation - 1);
    }

    @Test
    void shouldPlaceTwoInfluenceMarkersInDifferentRegions() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        RegionState region1 = game.getRegionByNumber(1);
        RegionState region2 = game.getRegionByNumber(2);

        region1.getFeatures().add(RegionFeature.IN_GAME);
        region2.getFeatures().add(RegionFeature.IN_GAME);

        region1.getInfluenceMarkers().add(new InfluenceMarker(playerId));
        region2.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        InfluenceAction action = new InfluenceAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.INFLUENCES)
        );

        action.start(context);

        action.handleDecision(
                context,
                new PlayerDecision(
                        new InfluenceDecision(List.of(1, 2))
                )
        );

        assertThat(region1.getInfluenceMarkers()).hasSize(2);
        assertThat(region2.getInfluenceMarkers()).hasSize(2);
        assertThat(player.getPopulation()).isEqualTo(2);
    }

    @Test
    void shouldAllowPlacementBecauseOfOwnedUnit() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        region.getUnits().add(
                new Unit(playerId, 1, region.getId())
        );

        InfluenceAction action = new InfluenceAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.INFLUENCES)
        );

        action.start(context);

        action.handleDecision(
                context,
                new PlayerDecision(
                        new InfluenceDecision(List.of(1))
                )
        );

        assertThat(region.getInfluenceMarkers())
                .anyMatch(marker -> marker.getPlayerId().equals(playerId));
    }

    @Test
    void shouldThrowWhenNoInfluenceAndNoUnit() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        InfluenceAction action = new InfluenceAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.INFLUENCES)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(
                                new InfluenceDecision(List.of(1))
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

        RegionState region1 = game.getRegionByNumber(1);
        RegionState region2 = game.getRegionByNumber(2);

        region1.getFeatures().add(RegionFeature.IN_GAME);
        region2.getFeatures().add(RegionFeature.IN_GAME);

        region1.getInfluenceMarkers().add(new InfluenceMarker(playerId));
        region2.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        InfluenceAction action = new InfluenceAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.INFLUENCES)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(
                                new InfluenceDecision(List.of(1, 2))
                        )
                )
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenRegionIsNotActive() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState region = game.getRegionByNumber(1);

        // specjalnie bez IN_GAME

        region.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        InfluenceAction action = new InfluenceAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.INFLUENCES)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(
                                new InfluenceDecision(List.of(1))
                        )
                )
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenRegionIsClosed() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState region = game.getRegionByNumber(1);

        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getFeatures().add(RegionFeature.CLOSED);

        region.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        InfluenceAction action = new InfluenceAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.INFLUENCES)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(
                                new InfluenceDecision(List.of(1))
                        )
                )
        ).isInstanceOf(IllegalStateException.class);
    }
}
