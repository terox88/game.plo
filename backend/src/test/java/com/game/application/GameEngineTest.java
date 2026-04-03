package com.game.game.application;

import com.game.game.application.action.AssignTokenToRegionAction;
import com.game.game.application.action.PlaceThornAction;
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

    @Test
    void shouldPlaceThornOnActiveRegion() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        RegionState region = game.getRegions().get(0);
        RegionToken token = game.getAvailableTokens().get(0);
        game.setCurrentPhase(Phase.SETUP_TOKENS);

        engine.assignToken(game, new AssignTokenToRegionAction(
                game.getCurrentPlayerId(),
                region.getId(),
                token.getId()
        ));

        game.setCurrentPhase(Phase.SETUP_THORN);

        var action = new PlaceThornAction(
                game.getCurrentPlayerId(),
                region.getId()
        );

        // when
        engine.placeThorn(game, action);

        // then
        assertThat(region.hasThorn()).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(Phase.SETUP_UROCZYSKA);
    }

    @Test
    void shouldDistributeUroczyskaOnlyOnValidRegions() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER),
                playerFactory.create("B", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);


        for (int i = 0; i < 4; i++) {
            var region = game.getRegions().get(i);
            var token = game.getAvailableTokens().get(0);

            game.setCurrentPhase(Phase.SETUP_TOKENS);
            engine.assignToken(game, new AssignTokenToRegionAction(
                    game.getCurrentPlayerId(),
                    region.getId(),
                    token.getId()
            ));
        }

        game.setCurrentPhase(Phase.SETUP_UROCZYSKA);

        // when
        engine.distributeUroczyska(game);

        // then
        long count = game.getRegions().stream()
                .flatMap(r -> r.getUroczyska().stream())
                .count();

        assertThat(count).isEqualTo(4);

        game.getRegions().forEach(region -> {
            if (region.hasThorn()) {
                assertThat(region.getUroczyska()).isEmpty();
            }
        });
    }
}