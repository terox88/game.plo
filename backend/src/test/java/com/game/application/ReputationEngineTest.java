package com.game.application;

import com.game.game.application.GameEngine;
import com.game.game.domain.GameState;
import com.game.game.domain.Hero;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;



public class ReputationEngineTest {
    private final GameSetupFactory gameFactory = new GameSetupFactory();
    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameEngine engine = new GameEngine();

    @Test
    void shouldDecreaseReputationAndPlaceOnTop() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        var engine = new GameEngine();

        UUID playerId = players.get(0).getPlayerId();

        // when
        engine.changeReputation(game, playerId, +1);

        // then
        assertThat(game.findPlayer(playerId).getReputation()).isEqualTo(1);

        var slot = game.getReputationTrack().getSlot(1);

        assertThat(slot.getPlayers().getFirst()).isEqualTo(playerId);
    }

    @Test
    void shouldIncreaseReputationAndPlaceAtBottom() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        var engine = new GameEngine();

        UUID playerId = players.get(0).getPlayerId();

        // najpierw pogorszenie (0 → 2)
        engine.changeReputation(game, playerId, +2);

        // potem poprawa (2 → 1)
        engine.changeReputation(game, playerId, -1);

        var slot = game.getReputationTrack().getSlot(1);

        // powinien być na dole
        assertThat(slot.getPlayers().getLast()).isEqualTo(playerId);
    }

    @Test
    void shouldMaintainOrderInSameSlot() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF),
                playerFactory.create("C", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        var engine = new GameEngine();

        UUID a = players.get(0).getPlayerId();
        UUID b = players.get(1).getPlayerId();
        UUID c = players.get(2).getPlayerId();

        engine.changeReputation(game, a, +1); // A -> 1
        engine.changeReputation(game, b, +1); // B -> 1
        engine.changeReputation(game, c, +1); // C -> 1

        var slot = game.getReputationTrack().getSlot(1);

        // ostatni na górze
        assertThat(slot.getPlayers()).containsExactly(c, b, a);
    }

    @Test
    void shouldNotAllowReturnToYinYang() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        var engine = new GameEngine();

        UUID playerId = players.get(0).getPlayerId();

        // 0 → 1
        engine.changeReputation(game, playerId, +1);

        // 1 → 0 (NIELEGALNE)
        assertThatThrownBy(() ->
                engine.changeReputation(game, playerId, -1)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldNotExceedMaxReputation() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        var engine = new GameEngine();

        UUID playerId = players.get(0).getPlayerId();


        engine.changeReputation(game, playerId, +10);

        assertThat(game.findPlayer(playerId).getReputation()).isEqualTo(10);


        assertThatThrownBy(() ->
                engine.changeReputation(game, playerId, +1)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldAllowStayingOnZero() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        var engine = new GameEngine();

        UUID playerId = players.get(0).getPlayerId();

        // delta 0
        engine.changeReputation(game, playerId, 0);

        assertThat(game.findPlayer(playerId).getReputation()).isEqualTo(0);
    }

    @Test
    void shouldRemovePlayerFromOldSlot() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        var engine = new GameEngine();

        UUID playerId = players.get(0).getPlayerId();

        engine.changeReputation(game, playerId, +1);

        assertThat(game.getReputationTrack().getSlot(0).getPlayers())
                .doesNotContain(playerId);
    }
}
