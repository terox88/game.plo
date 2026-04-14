package com.game.domain;

import com.game.game.domain.ActionFieldType;
import com.game.game.domain.ActionMarker;
import com.game.game.domain.GameState;
import com.game.game.domain.Hero;
import com.game.game.domain.action.*;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class MakingActionTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    @Test
    void shouldMoveManaCorrectly() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        // ustaw stan many
        var player = game.findPlayer(playerId);
        player.setMana(0, 3);
        player.setMana(3, 0);

        MakingAction action = new MakingAction();

        ActionMarker marker = new ActionMarker(playerId, ActionFieldType.MAKING);
        ActionContext context = new ActionContext(game, marker);

        action.start(context);

        // wybór MOVE
        action.handleDecision(context, new PlayerDecision(MakingChoice.MOVE));

        // ruch: 0 → 3 (1 mana = koszt 3)
        action.handleDecision(context, new PlayerDecision(
                new MoveManaDecision(0, 3, 1)
        ));

        assertThat(player.getMana(0)).isEqualTo(2);
        assertThat(player.getMana(3)).isEqualTo(1);
    }

    @Test
    void shouldConsumeCorrectMovePoints() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        var player = game.findPlayer(playerId);

        player.setMana(0, 5);

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);

        action.handleDecision(context, new PlayerDecision(MakingChoice.MOVE));

        // 2 many z 0 → 2 = koszt 4
        action.handleDecision(context,
                new PlayerDecision(new MoveManaDecision(0, 2, 2))
        );

        // zostało 1 punkt → można jeszcze coś zrobić
        assertThat(context.getState().get("movePoints")).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenNotEnoughMovePoints() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        var player = game.findPlayer(playerId);

        player.setMana(0, 5);

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);

        action.handleDecision(context, new PlayerDecision(MakingChoice.MOVE));

        // koszt 6 (>5)
        assertThatThrownBy(() ->
                action.handleDecision(context,
                        new PlayerDecision(new MoveManaDecision(0, 3, 2))
                )
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldAllowPassAndFinishMove() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);

        action.handleDecision(context, new PlayerDecision(MakingChoice.MOVE));

        // PASS
        var result = action.handleDecision(context, new PlayerDecision(MakingChoice.PASS));

        assertThat(result.requiresDecision()).isTrue(); // wraca do wyboru akcji
    }

    @Test
    void shouldApplyMoveStartEffects() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);

        action.handleDecision(context, new PlayerDecision(MakingChoice.MOVE));

        // reputacja +1
        assertThat(game.findPlayer(playerId).getReputation()).isEqualTo(1);

        // dead snow +1
        assertThat(game.getDeadSnow()).isEqualTo(1);
    }
}
