package com.game.domain;

import com.game.game.application.GameEngine;
import com.game.game.domain.GameState;
import com.game.game.domain.Hero;
import com.game.game.domain.ReputationService;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;



public class ReputationServiceTest {
    private final GameSetupFactory gameFactory = new GameSetupFactory();
    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final ReputationService service = new ReputationService();

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
       service.changeReputation(game, playerId, +1);

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

        UUID playerId = players.get(0).getPlayerId();

        // najpierw pogorszenie (0 → 2)
        service.changeReputation(game, playerId, +2);

        // potem poprawa (2 → 1)
        service.changeReputation(game, playerId, -1);

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


        UUID a = players.get(0).getPlayerId();
        UUID b = players.get(1).getPlayerId();
        UUID c = players.get(2).getPlayerId();

        service.changeReputation(game, a, +1); // A -> 1
        service.changeReputation(game, b, +1); // B -> 1
        service.changeReputation(game, c, +1); // C -> 1

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


        UUID playerId = players.get(0).getPlayerId();

        // 0 → 1
        service.changeReputation(game, playerId, +1);

        // 1 → 0 (NIELEGALNE)
        assertThatThrownBy(() ->
               service.changeReputation(game, playerId, -1)
        ).isInstanceOf(IllegalStateException.class);
    }


    @Test
    void shouldAllowStayingOnZero() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);


        UUID playerId = players.get(0).getPlayerId();

        // delta 0
        service.changeReputation(game, playerId, 0);

        assertThat(game.findPlayer(playerId).getReputation()).isEqualTo(0);
    }

    @Test
    void shouldRemovePlayerFromOldSlot() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);


        UUID playerId = players.get(0).getPlayerId();

        service.changeReputation(game, playerId, +1);

        assertThat(game.getReputationTrack().getSlot(0).getPlayers())
                .doesNotContain(playerId);
    }

    @Test
    void shouldStayOnMaxLevelAndMoveToTopWhenExceedingMax() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID a = players.get(0).getPlayerId();
        UUID b = players.get(1).getPlayerId();

        // doprowadzamy obu na max level (10)
        service.changeReputation(game, a, +10);
        service.changeReputation(game, b, +10);

        var slot = game.getReputationTrack().getSlot(10);

        // kolejność: B na górze, potem A (bo B był ostatni)
        assertThat(slot.getPlayers()).containsExactly(b, a);

        // teraz A dostaje +1 (overflow)
        service.changeReputation(game, a, +1);

        // nadal level 10
        assertThat(game.findPlayer(a).getReputation()).isEqualTo(10);

        // A powinien być teraz NA GÓRZE (najgorszy)
        assertThat(slot.getPlayers().getFirst()).isEqualTo(a);
    }

    @Test
    void shouldKeepPlayerAtTopWhenAlreadyMaxAndWorseningAgain() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        service.changeReputation(game, playerId, +10);

        var slot = game.getReputationTrack().getSlot(10);

        // pierwszy raz
        assertThat(slot.getPlayers().getFirst()).isEqualTo(playerId);

        // drugi raz overflow
        service.changeReputation(game, playerId, +1);

        // nadal na topie
        assertThat(slot.getPlayers().getFirst()).isEqualTo(playerId);
    }
}
