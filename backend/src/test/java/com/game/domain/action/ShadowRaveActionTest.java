package com.game.domain.action;
import com.game.game.domain.*;
import com.game.game.domain.action.*;
import com.game.game.domain.service.ReputationService;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
public class ShadowRaveActionTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    @Test
    void shouldReduceVukoTokenAndSpendMana() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setVukoTokens(2);
        player.setMana(0, 2);

        ShadowRaveAction action = new ShadowRaveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.SHADOW_RAVE)
        );

        action.start(context);

        action.handleDecision(
                context,
                new PlayerDecision(ShadowRaveChoice.REDUCE_VUKO)
        );

        assertThat(player.getVukoTokens()).isEqualTo(1);
        assertThat(player.getMana(0)).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenNoVukoTokensToReduce() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setVukoTokens(0);
        player.setMana(0, 2);

        ShadowRaveAction action = new ShadowRaveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.SHADOW_RAVE)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(ShadowRaveChoice.REDUCE_VUKO)
                )
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenNoManaToReduceVuko() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setVukoTokens(2);
        player.setMana(0, 0);

        ShadowRaveAction action = new ShadowRaveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.SHADOW_RAVE)
        );

        action.start(context);

        assertThatThrownBy(() ->
                action.handleDecision(
                        context,
                        new PlayerDecision(ShadowRaveChoice.REDUCE_VUKO)
                )
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldImproveReputationByTwoAndPlaceAtBottom() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerA = players.get(0).getPlayerId();
        UUID playerB = players.get(1).getPlayerId();

        ReputationService reputationService = new ReputationService();

        // obaj na level 3
        reputationService.changeReputation(game, playerA, +3);
        reputationService.changeReputation(game, playerB, +3);

        ShadowRaveAction action = new ShadowRaveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerA, ActionFieldType.SHADOW_RAVE)
        );

        action.start(context);

        action.handleDecision(
                context,
                new PlayerDecision(ShadowRaveChoice.IMPROVE_REPUTATION)
        );

        assertThat(game.findPlayer(playerA).getReputation()).isEqualTo(1);

        var slot = game.getReputationTrack().getSlot(1);

        assertThat(slot.getPlayers().getLast()).isEqualTo(playerA);
    }

    @Test
    void shouldStopAtOneAndNotReturnToZero() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        ReputationService reputationService = new ReputationService();

        // 0 -> 2
        reputationService.changeReputation(game, playerId, +2);

        ShadowRaveAction action = new ShadowRaveAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.SHADOW_RAVE)
        );

        action.start(context);

        action.handleDecision(
                context,
                new PlayerDecision(ShadowRaveChoice.IMPROVE_REPUTATION)
        );

        assertThat(game.findPlayer(playerId).getReputation()).isEqualTo(1);
    }
}
