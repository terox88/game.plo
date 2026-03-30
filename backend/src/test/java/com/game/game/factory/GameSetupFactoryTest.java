package com.game.game.factory;

import com.game.game.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameSetupFactoryTest {

    private final GameSetupFactory gameFactory = new GameSetupFactory();
    private final PlayerStateFactory playerFactory = new PlayerStateFactory();

    @Test
    void shouldCreateGameWithProperInitialState() {

        // given
        List<PlayerState> players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        // when
        GameState game = gameFactory.createGame(players);

        // then

        // 👥 players
        assertThat(game.getPlayers()).hasSize(2);

        // 🌍 regions (zawsze 8)
        assertThat(game.getRegions()).hasSize(8);

        // 🎯 action fields
        assertThat(game.getActionFields()).hasSize(5);

        // 🐍 viper gorge
        assertThat(game.getViperGorge()).isNotNull();

        // 🎯 tokeny dostępne
        assertThat(game.getAvailableTokens()).isNotEmpty();

        // 🔄 inicjatywa
        assertThat(game.getInitiativeOrder()).hasSize(2);

        // 👤 current player = pierwszy z listy
        assertThat(game.getCurrentPlayerId())
                .isEqualTo(players.get(0).getPlayerId());

        // 🎮 faza
        assertThat(game.getCurrentPhase())
                .isEqualTo(Phase.INITIATIVE);

        // ❄️ dead snow
        assertThat(game.getDeadSnow()).isEqualTo(0);

        // 🔁 stage / round
        assertThat(game.getStageNumber()).isEqualTo(1);
        assertThat(game.getRoundNumber()).isEqualTo(1);
    }

    @Test
    void shouldCreateRegionsWithoutAssignedTokens() {

        // given
        List<PlayerState> players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        // when
        GameState game = gameFactory.createGame(players);

        // then

        long assignedTokens = game.getRegions().stream()
                .filter(r -> r.getLandToken() != null)
                .count();

        assertThat(assignedTokens).isEqualTo(0);
    }

    @Test
    void shouldCreateRegionsWithNeighbors() {

        // given
        List<PlayerState> players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        // when
        GameState game = gameFactory.createGame(players);

        // then
        game.getRegions().forEach(region ->
                assertThat(region.getNeighbors()).isNotEmpty()
        );
    }

    @Test
    void shouldThrowExceptionWhenNoPlayersProvided() {

        // when + then
        assertThrows(IllegalArgumentException.class, () ->
                gameFactory.createGame(List.of())
        );
    }
}