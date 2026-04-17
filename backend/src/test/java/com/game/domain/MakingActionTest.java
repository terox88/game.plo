package com.game.domain;

import com.game.game.domain.*;
import com.game.game.domain.action.*;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
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

    @Test
    void shouldSummonUnitCorrectly() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(1, 2);

        RegionState region = game.getRegions().get(0);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);

        action.handleDecision(context, new PlayerDecision(MakingChoice.SUMMON));

        action.handleDecision(context, new PlayerDecision(
                new SummonDecision(1, Map.of(region.getNumber(), 1))
        ));

        assertThat(region.getUnits()).hasSize(1);
        assertThat(player.getUnitLevel1()).isEqualTo(2); // 3 - 1
    }

    @Test
    void shouldThrowWhenNotEnoughMana() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(1, 0);

        RegionState region = game.getRegions().get(0);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.SUMMON));

        assertThatThrownBy(() ->
                action.handleDecision(context, new PlayerDecision(
                        new SummonDecision(1, Map.of(region.getNumber(), 1))
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenNoInfluenceOrUnitInRegion() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(1, 2);

        RegionState region = game.getRegions().get(0);
        region.getFeatures().add(RegionFeature.IN_GAME);

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.SUMMON));

        assertThatThrownBy(() ->
                action.handleDecision(context, new PlayerDecision(
                        new SummonDecision(1, Map.of(region.getNumber(), 1))
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldSummonToMultipleRegions() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(1, 3);

        RegionState r1 = game.getRegions().get(0);
        RegionState r2 = game.getRegions().get(1);
        r1.getFeatures().add(RegionFeature.IN_GAME);
        r2.getFeatures().add(RegionFeature.IN_GAME);

        r1.getInfluenceMarkers().add(new InfluenceMarker(playerId));
        r2.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.SUMMON));

        action.handleDecision(context, new PlayerDecision(
                new SummonDecision(1, Map.of(
                        r1.getNumber(), 1,
                        r2.getNumber(), 1
                ))
        ));

        assertThat(r1.getUnits()).hasSize(1);
        assertThat(r2.getUnits()).hasSize(1);
    }

    @Test
    void shouldApplyVanDykenBonus() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER) // VanDyken
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(1, 1);

        RegionState region = game.getRegions().get(0);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.SUMMON));

        action.handleDecision(context, new PlayerDecision(
                new SummonDecision(1, Map.of(region.getNumber(), 1))
        ));

        // 1 * level(1) + 2 bonus = 3
        assertThat(player.getReputation()).isEqualTo(3);
    }

    @Test
    void shouldIncreaseDeadSnow() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(1, 1);

        RegionState region = game.getRegions().get(0);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.SUMMON));

        action.handleDecision(context, new PlayerDecision(
                new SummonDecision(1, Map.of(region.getNumber(), 1))
        ));

        assertThat(game.getDeadSnow()).isEqualTo(1);
    }

    @Test
    void shouldAllowSkippingSummon() {

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

        action.handleDecision(context, new PlayerDecision(MakingChoice.SUMMON));

        var result = action.handleDecision(context, new PlayerDecision(MakingChoice.PASS));

        assertThat(result.requiresDecision()).isTrue();
    }

    @Test
    void shouldThrowWhenRegionIsNotActive() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(1, 1);

        RegionState region = game.getRegions().get(0);

        region.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.SUMMON));

        assertThatThrownBy(() ->
                action.handleDecision(context, new PlayerDecision(
                        new SummonDecision(1, Map.of(region.getNumber(), 1))
                ))
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active region");
    }

    @Test
    void shouldAllowSummonOnActiveRegion() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(1, 1);

        RegionState region = game.getRegions().get(0);

        region.getFeatures().add(RegionFeature.IN_GAME);

        region.getInfluenceMarkers().add(new InfluenceMarker(playerId));

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.SUMMON));

        action.handleDecision(context, new PlayerDecision(
                new SummonDecision(1, Map.of(region.getNumber(), 1))
        ));

        assertThat(region.getUnits()).hasSize(1);
    }

    @Test
    void shouldAddAbilityWhenFreeSlotAvailable() {

        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(0, 2);

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);

        action.handleDecision(context, new PlayerDecision(MakingChoice.UPGRADE));

        action.handleDecision(context, new PlayerDecision(
                new UpgradeDecision(1, AbilitiesType.SPEED, null)
        ));

        assertThat(player.getLevel1().getAbilities())
                .contains(AbilitiesType.SPEED);
    }

    @Test
    void shouldThrowWhenNotEnoughManaForUpgrade() {

        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.UPGRADE));

        assertThatThrownBy(() ->
                action.handleDecision(context, new PlayerDecision(
                        new UpgradeDecision(1, AbilitiesType.SPEED, null)
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenDuplicateAbilityOnLevel1Or2() {

        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(0, 2);

        // OLAF ma już ATTACK na level 1
        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.UPGRADE));

        assertThatThrownBy(() ->
                action.handleDecision(context, new PlayerDecision(
                        new UpgradeDecision(1, AbilitiesType.ATTACK, null)
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenVanDykenLevel1Upgrade() {

        var players = List.of(
                playerFactory.create("A", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(0, 2);

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.UPGRADE));

        assertThatThrownBy(() ->
                action.handleDecision(context, new PlayerDecision(
                        new UpgradeDecision(1, AbilitiesType.SPEED, null)
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldReplaceAbilityWhenNoFreeSlots() {

        var players = List.of(
                playerFactory.create("A", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(0, 2);

        var abilities = player.getLevel1();

        // wypełniamy sloty ręcznie
        abilities.addAbility(AbilitiesType.SPEED);

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.UPGRADE));

        action.handleDecision(context, new PlayerDecision(
                new UpgradeDecision(1, AbilitiesType.SHIELD, AbilitiesType.SPEED)
        ));

        assertThat(abilities.getAbilities())
                .contains(AbilitiesType.SHIELD)
                .doesNotContain(AbilitiesType.SPEED);
    }

    @Test
    void shouldNotReplaceBaseAbility() {

        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(0, 2);

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.UPGRADE));

        // ATTACK jest nadrukowane
        assertThatThrownBy(() ->
                action.handleDecision(context, new PlayerDecision(
                        new UpgradeDecision(1, AbilitiesType.SPEED, AbilitiesType.ATTACK)
                ))
        ).isInstanceOf(IllegalStateException.class);
    }
    @Test
    void shouldApplyUpgradeEffects() {

        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.setMana(0, 2);

        MakingAction action = new MakingAction();

        ActionContext context = new ActionContext(
                game,
                new ActionMarker(playerId, ActionFieldType.MAKING)
        );

        action.start(context);
        action.handleDecision(context, new PlayerDecision(MakingChoice.UPGRADE));

        action.handleDecision(context, new PlayerDecision(
                new UpgradeDecision(1, AbilitiesType.SPEED, null)
        ));

        assertThat(game.getDeadSnow()).isEqualTo(1);
        assertThat(player.getReputation()).isEqualTo(1);
    }
}
