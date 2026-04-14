package com.game.application;

import com.game.game.application.GameEngine;
import com.game.game.application.command.AdvanceInitiativeAction;
import com.game.game.domain.Phase;
import com.game.game.domain.PlayerState;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import com.game.game.domain.GameState;
import com.game.game.domain.Hero;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class InitiativeTrackTest {

    private final GameSetupFactory gameFactory = new GameSetupFactory();
    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameEngine engine = new GameEngine();


    @Test
    void shouldReturnInitialOrderFromTrack() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF),
                playerFactory.create("C", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        var order = game.getInitiativeTurnOrder();

        assertThat(order).containsExactly(
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId(),
                players.get(2).getPlayerId()
        );
    }

    @Test
    void shouldMovePlayerByOne() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        UUID playerA = players.get(0).getPlayerId();

        game.setCurrentPhase(Phase.INITIATIVE);

        engine.advanceInitiative(game, new AdvanceInitiativeAction(playerA, 1));

        var slot = game.getInitiativeTrack().findPlayerSlot(playerA);

        assertThat(slot.getPosition()).isEqualTo(1);
    }

    @Test
    void shouldMovePlayerByTwo() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        UUID playerA = players.get(0).getPlayerId();

        game.setCurrentPhase(Phase.INITIATIVE);

        engine.advanceInitiative(game, new AdvanceInitiativeAction(playerA, 2));

        var slot = game.getInitiativeTrack().findPlayerSlot(playerA);

        assertThat(slot.getPosition()).isEqualTo(2);
    }


    @Test
    void shouldOrderByStageThenRound() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        UUID a = players.get(0).getPlayerId();
        UUID b = players.get(1).getPlayerId();

        game.setCurrentPhase(Phase.INITIATIVE);

        // A robi 2 kroki (stage1 round3)
        engine.advanceInitiative(game, new AdvanceInitiativeAction(a, 2));

        // B robi więcej → wchodzi w stage2
        engine.advanceInitiative(game, new AdvanceInitiativeAction(b, 2));
        game.setCurrentPlayerId(b);
        game.setCurrentPhase(Phase.INITIATIVE);
        engine.advanceInitiative(game, new AdvanceInitiativeAction(b, 1));

        var order = game.getCurrentTurnOrder();

        // 🔥 B pierwszy (wyższy stage)
        assertThat(order.get(0)).isEqualTo(b);
    }

    @Test
    void shouldMaintainOrderInsideSameSlot() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF),
                playerFactory.create("C", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        game.setCurrentPhase(Phase.INITIATIVE);

        UUID a = players.get(0).getPlayerId();
        UUID b = players.get(1).getPlayerId();
        UUID c = players.get(2).getPlayerId();
        engine.startInitiativePhase(game);

        // wszyscy na slot 1
        engine.advanceInitiative(game, new AdvanceInitiativeAction(a, 1));
        engine.advanceInitiative(game, new AdvanceInitiativeAction(b, 1));
        engine.advanceInitiative(game, new AdvanceInitiativeAction(c, 1));

        var slot = game.getInitiativeTrack().getSlots().get(1);

        // ostatni na końcu (kolejność wejścia)
        assertThat(slot.getPlayers()).containsExactly(c, b, a);
    }

    @Test
    void shouldNotAllowDoubleMoveTwiceInSameStage() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        UUID playerId = players.get(0).getPlayerId();

        game.setCurrentPhase(Phase.INITIATIVE);
        engine.startInitiativePhase(game);

        engine.advanceInitiative(game, new AdvanceInitiativeAction(playerId, 2));

        assertThatThrownBy(() ->
                engine.advanceInitiative(game, new AdvanceInitiativeAction(playerId, 2))
        ).isInstanceOf(IllegalStateException.class);
    }
    @Test
    void shouldUpdatePlayerStageAndRound() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);
        var engine = new GameEngine();

        UUID playerId = players.get(0).getPlayerId();

        game.setCurrentPhase(Phase.INITIATIVE);
        engine.startInitiativePhase(game);

        engine.advanceInitiative(game, new AdvanceInitiativeAction(playerId, 1));

        PlayerState player = game.findPlayer(playerId);

        assertThat(player.getRound()).isEqualTo(2);
    }
}
