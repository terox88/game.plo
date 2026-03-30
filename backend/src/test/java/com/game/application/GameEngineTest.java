package com.game.game.application;

import com.game.game.application.action.AssignTokenToRegionAction;
import com.game.game.domain.*;
import com.game.game.factory.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameEngineTest {

    private final GameSetupFactory gameFactory = new GameSetupFactory();
    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameEngine engine = new GameEngine();

    @Test
    void shouldAssignTokenToRegion() {

        // given
        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        RegionState region = game.getRegions().get(0);
        RegionToken token = game.getAvailableTokens().get(0);

        var action = new AssignTokenToRegionAction(
                game.getCurrentPlayerId(),
                region.getId(),
                token.getId()
        );

        // when
        engine.assignToken(game, action);

        // then
        assertThat(region.getLandToken()).isEqualTo(token);
        assertThat(game.getAvailableTokens()).doesNotContain(token);
    }
}