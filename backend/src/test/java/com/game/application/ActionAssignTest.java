package com.game.application;

import com.game.game.application.GameEngine;
import com.game.game.application.action.AssignActionOrderAction;
import com.game.game.application.action.PlaceActionMarkerOnFieldAction;
import com.game.game.application.action.PlaceActionMarkerOnViperGorgeAction;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import com.game.game.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ActionAssignTest {
    private final GameSetupFactory gameFactory = new GameSetupFactory();
    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameEngine engine = new GameEngine();

    @Test
    void shouldAssignOrderToFields() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        game.setCurrentPhase(Phase.PLANNING_ORDER);

        UUID a = players.get(0).getPlayerId();

        engine.assignActionOrder(game,
                new AssignActionOrderAction(a, ActionFieldType.MOVE, 3));

        assertThat(game.getActionOrderAssignments())
                .containsEntry(ActionFieldType.MOVE, 3);
    }

    @Test
    void shouldNotAllowDuplicateOrderNumber() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        game.setCurrentPhase(Phase.PLANNING_ORDER);

        UUID a = players.get(0).getPlayerId();
        UUID b = players.get(1).getPlayerId();

        engine.assignActionOrder(game,
                new AssignActionOrderAction(a, ActionFieldType.MOVE, 3));

        assertThatThrownBy(() ->
                engine.assignActionOrder(game,
                        new AssignActionOrderAction(b, ActionFieldType.MOUNTAIN, 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldAutoAssignLastField() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        game.setCurrentPhase(Phase.PLANNING_ORDER);

        UUID a = players.get(0).getPlayerId();
        UUID b = players.get(1).getPlayerId();

        engine.assignActionOrder(game, new AssignActionOrderAction(a, ActionFieldType.MOVE, 1));
        engine.assignActionOrder(game, new AssignActionOrderAction(b, ActionFieldType.MOUNTAIN, 2));
        engine.assignActionOrder(game, new AssignActionOrderAction(a, ActionFieldType.MAKING, 3));
        engine.assignActionOrder(game, new AssignActionOrderAction(b, ActionFieldType.SHADOW_RAVE, 4));

        // 🔥 ostatni powinien się przypisać automatycznie
        assertThat(game.getActionOrderAssignments().size()).isEqualTo(5);

        assertThat(game.getActionOrderAssignments().values())
                .containsExactlyInAnyOrder(1,2,3,4,5);
    }

    @Test
    void shouldPlaceActionMarkerOnField() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        game.setCurrentPhase(Phase.PLANNING_ACTIONS);

        PlayerState player = players.get(0);
        player.resetActionMarkersForRound();

        engine.placeActionMarker(game,
                new PlaceActionMarkerOnFieldAction(player.getPlayerId(), ActionFieldType.MOVE));

        var field = game.getActionFields().stream()
                .filter(f -> f.getType() == ActionFieldType.MOVE)
                .findFirst()
                .orElseThrow();

        assertThat(field.getMarkers()).hasSize(1);
    }

    @Test
    void shouldPlaceActionAndInfluenceOnViperGorge() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        game.setCurrentPhase(Phase.PLANNING_ACTIONS);

        PlayerState player = players.get(0);
        player.resetActionMarkersForRound();
        player.setAvailableInfluenceMarkers(5);

        engine.placeOnViperGorge(game,
                new PlaceActionMarkerOnViperGorgeAction(player.getPlayerId()));

        assertThat(game.getViperGorge().getActionMarkers()).hasSize(1);
        assertThat(game.getViperGorge().getInfluenceMarkers()).hasSize(1);
    }

    @Test
    void shouldNotPlaceOnViperGorgeWithoutInfluence() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        game.setCurrentPhase(Phase.PLANNING_ACTIONS);

        PlayerState player = players.get(0);
        player.resetActionMarkersForRound();
        player.setAvailableInfluenceMarkers(0);

        assertThatThrownBy(() ->
                engine.placeOnViperGorge(game,
                        new PlaceActionMarkerOnViperGorgeAction(player.getPlayerId()))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldSkipPlayerWithoutMarkers() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        game.setCurrentPhase(Phase.PLANNING_ACTIONS);

        PlayerState a = players.get(0);
        PlayerState b = players.get(1);

        a.setAvailableActionMarkers(0); // 🔥 brak
        b.setAvailableActionMarkers(2);

        game.setCurrentPlayerId(a.getPlayerId());

        engine.placeActionMarker(game,
                new PlaceActionMarkerOnFieldAction(b.getPlayerId(), ActionFieldType.MOVE));

        assertThat(game.getCurrentPlayerId()).isEqualTo(b.getPlayerId());
    }

    @Test
    void shouldMoveToActionPhaseWhenAllMarkersUsed() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        game.setCurrentPhase(Phase.PLANNING_ACTIONS);

        PlayerState player = players.get(0);
        player.setAvailableActionMarkers(1);

        engine.placeActionMarker(game,
                new PlaceActionMarkerOnFieldAction(player.getPlayerId(), ActionFieldType.MOVE));

        assertThat(game.getCurrentPhase()).isEqualTo(Phase.ACTION);
    }

}
