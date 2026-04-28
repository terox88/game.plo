package com.game.domain.action;
import com.game.game.domain.*;
import com.game.game.domain.action.*;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
public class ViperGorgeActionTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();


    @Test
    void shouldTakeOneGoldFromViperGorge() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        PlayerState player = players.get(0);

        ViperGorge viperGorge = new ViperGorge();
        viperGorge.addActionMarker(
                new ActionMarker(player.getPlayerId(), ActionFieldType.VIPER_GORGE)
        );

        ViperGorgeResolver resolver = new ViperGorgeResolver();

        int beforeGold = player.getGold();

        ViperGorgeDecision decision = new ViperGorgeDecision(
                List.of(ResourceType.GOLD),
                List.of()
        );

        resolver.resolvePlayer(player, viperGorge, decision);

        assertThat(player.getGold())
                .isEqualTo(beforeGold + 1);
    }

    @Test
    void shouldTakeTwoResourcesWithTwoMarkers() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        PlayerState player = players.get(0);

        ViperGorge viperGorge = new ViperGorge();

        viperGorge.addActionMarker(
                new ActionMarker(player.getPlayerId(), ActionFieldType.VIPER_GORGE)
        );

        viperGorge.addActionMarker(
                new ActionMarker(player.getPlayerId(), ActionFieldType.VIPER_GORGE)
        );

        ViperGorgeResolver resolver = new ViperGorgeResolver();

        int beforeGold = player.getGold();
        int beforePopulation = player.getPopulation();

        ViperGorgeDecision decision = new ViperGorgeDecision(
                List.of(
                        ResourceType.GOLD,
                        ResourceType.POPULATION
                ),
                List.of()
        );

        resolver.resolvePlayer(player, viperGorge, decision);

        assertThat(player.getGold())
                .isEqualTo(beforeGold + 1);

        assertThat(player.getPopulation())
                .isEqualTo(beforePopulation + 1);
    }

    @Test
    void shouldThrowWhenTakingMoreResourcesThanMarkers() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        PlayerState player = players.get(0);

        ViperGorge viperGorge = new ViperGorge();

        viperGorge.addActionMarker(
                new ActionMarker(player.getPlayerId(), ActionFieldType.VIPER_GORGE)
        );

        ViperGorgeResolver resolver = new ViperGorgeResolver();

        ViperGorgeDecision decision = new ViperGorgeDecision(
                List.of(
                        ResourceType.GOLD,
                        ResourceType.MANA
                ),
                List.of()
        );

        assertThatThrownBy(() ->
                resolver.resolvePlayer(player, viperGorge, decision)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldExchangeTwoResourcesForOneMana() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        PlayerState player = players.get(0);

        player.setGold(2);
        player.setPopulation(2);
        player.setMana(0, 0);

        ViperGorge viperGorge = new ViperGorge();
        viperGorge.addActionMarker(
                new ActionMarker(player.getPlayerId(), ActionFieldType.VIPER_GORGE)
        );

        ViperGorgeResolver resolver = new ViperGorgeResolver();

        int beforeGold = player.getGold();
        int beforePopulation = player.getPopulation();

        ViperGorgeDecision decision = new ViperGorgeDecision(
                List.of(),
                List.of(
                        new ExchangeDecision(
                                ResourceType.GOLD,
                                ResourceType.POPULATION,
                                ResourceType.MANA
                        )
                )
        );

        resolver.resolvePlayer(player, viperGorge, decision);

        assertThat(player.getGold())
                .isEqualTo(beforeGold - 1);

        assertThat(player.getPopulation())
                .isEqualTo(beforePopulation - 1);

        assertThat(player.getMana(0))
                .isEqualTo(1);
    }

    @Test
    void shouldThrowWhenNotEnoughResourcesForExchange() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        PlayerState player = players.get(0);

        player.setGold(0);
        player.setPopulation(1);

        ViperGorge viperGorge = new ViperGorge();
        viperGorge.addActionMarker(
                new ActionMarker(player.getPlayerId(), ActionFieldType.VIPER_GORGE)
        );

        ViperGorgeResolver resolver = new ViperGorgeResolver();

        ViperGorgeDecision decision = new ViperGorgeDecision(
                List.of(),
                List.of(
                        new ExchangeDecision(
                                ResourceType.GOLD,
                                ResourceType.POPULATION,
                                ResourceType.MANA
                        )
                )
        );

        assertThatThrownBy(() ->
                resolver.resolvePlayer(player, viperGorge, decision)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldValidateMultipleExchangesGlobally() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        PlayerState player = players.get(0);

        player.setGold(1);
        player.setPopulation(1);
        player.setMana(0, 0);

        ViperGorge viperGorge = new ViperGorge();
        viperGorge.addActionMarker(
                new ActionMarker(player.getPlayerId(), ActionFieldType.VIPER_GORGE)
        );

        ViperGorgeResolver resolver = new ViperGorgeResolver();

        ViperGorgeDecision decision = new ViperGorgeDecision(
                List.of(),
                List.of(
                        new ExchangeDecision(
                                ResourceType.GOLD,
                                ResourceType.POPULATION,
                                ResourceType.MANA
                        ),
                        new ExchangeDecision(
                                ResourceType.GOLD,
                                ResourceType.POPULATION,
                                ResourceType.MANA
                        )
                )
        );

        assertThatThrownBy(() ->
                resolver.resolvePlayer(player, viperGorge, decision)
        ).isInstanceOf(IllegalStateException.class);
    }
}
