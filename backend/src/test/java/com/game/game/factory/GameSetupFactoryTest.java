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

        // 🌍 regions
        assertThat(game.getRegions()).hasSize(8);

        // 🎯 action fields
        assertThat(game.getActionFields()).hasSize(5);

        // 🐍 viper gorge
        assertThat(game.getViperGorge()).isNotNull();

        // 🎯 tokeny
        assertThat(game.getAvailableTokens()).isNotEmpty();

        // 🧠 initiative track
        assertThat(game.getInitiativeTrack()).isNotNull();
        assertThat(game.getInitiativeTrack().getSlots()).isNotEmpty();

        // 👤 current player (pierwszy na tracku)
        assertThat(game.getCurrentPlayerId()).isNotNull();

        // 🎮 faza startowa (setup!)
        assertThat(game.getCurrentPhase())
                .isEqualTo(Phase.SETUP_TOKENS);

        // ❄️ dead snow
        assertThat(game.getDeadSnow()).isEqualTo(0);

        // 🧠 global progress
        assertThat(game.getStageLast()).isEqualTo(1);
        assertThat(game.getRoundLast()).isEqualTo(1);

        // 👤 players state (ważne!)
        game.getPlayers().forEach(player -> {
            assertThat(player.getStage()).isEqualTo(1);
            assertThat(player.getRound()).isEqualTo(1);
            assertThat(player.isUsedDoubleMoveInStage()).isFalse();
        });
        assertThat(
                game.getInitiativeTrack().getTurnOrder()
        ).containsExactlyElementsOf(
                players.stream().map(PlayerState::getPlayerId).toList()
        );
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