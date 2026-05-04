package com.game.domain.unitactivation;

import com.game.game.domain.*;
import com.game.game.domain.unitactivation.*;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class MakerSpyResolverTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    private final MakerSpyResolver resolver = new MakerSpyResolver();

    @Test
    void shouldReplaceTwoMakerSpyAbilities() {
        var players = List.of(
                playerFactory.create("A", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);


        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);

        Unit harrasim = new Unit(playerId, 3, region.getId());
        region.getUnits().add(harrasim);

        resolver.resolve(game, new MakerSpyDecision(
                playerId,
                AbilitiesType.ATTACK,
                AbilitiesType.SPEED
        ));

        assertThat(player.getLevel3().getAbilities())
                .contains(AbilitiesType.ATTACK, AbilitiesType.SPEED);

        assertThat(player.getLevel3().getAbilities())
                .doesNotContain(AbilitiesType.MAKER_SPY);

        assertThat(game.getReplacedMakerSpyIndexesByPlayer())
                .containsKey(playerId);
    }

    @Test
    void shouldRejectSameAbilities() {
        var players = List.of(
                playerFactory.create("A", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getUnits().add(new Unit(playerId, 3, region.getId()));

        assertThatThrownBy(() ->
                resolver.resolve(game, new MakerSpyDecision(
                        playerId,
                        AbilitiesType.ATTACK,
                        AbilitiesType.ATTACK
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectInvalidAbility() {
        var players = List.of(
                playerFactory.create("A", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getUnits().add(new Unit(playerId, 3, region.getId()));

        assertThatThrownBy(() ->
                resolver.resolve(game, new MakerSpyDecision(
                        playerId,
                        AbilitiesType.ATTACK,
                        AbilitiesType.FAUN
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRestoreOnlyReplacedMakerSpySlots() {
        var players = List.of(
                playerFactory.create("A", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.getLevel3().getAbilities().clear();
        player.getLevel3().getAbilities().addAll(List.of(
                AbilitiesType.MAKER_SPY,
                AbilitiesType.MAKER_SPY,
                AbilitiesType.ATTACK
        ));

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getUnits().add(new Unit(playerId, 3, region.getId()));

        resolver.resolve(game, new MakerSpyDecision(
                playerId,
                AbilitiesType.SPEED,
                AbilitiesType.SHIELD
        ));

        player.getLevel3().addAbility(AbilitiesType.DOMINATION);

        resolver.restore(game);

        assertThat(player.getLevel3().getAbilities())
                .containsExactly(
                        AbilitiesType.MAKER_SPY,
                        AbilitiesType.MAKER_SPY,
                        AbilitiesType.ATTACK,
                        AbilitiesType.DOMINATION
                );

        assertThat(game.getReplacedMakerSpyIndexesByPlayer()).isEmpty();
    }

    @Test
    void shouldNotResolveIfHarrasimIsNotOnBoard() {
        var players = List.of(
                playerFactory.create("A", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();


        assertThat(resolver.canResolve(game, playerId)).isFalse();

        assertThatThrownBy(() ->
                resolver.resolve(game, new MakerSpyDecision(
                        playerId,
                        AbilitiesType.ATTACK,
                        AbilitiesType.SPEED
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldNotResolveForDifferentHero() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        player.getLevel3().getAbilities().clear();
        player.getLevel3().getAbilities().addAll(List.of(
                AbilitiesType.MAKER_SPY,
                AbilitiesType.MAKER_SPY
        ));

        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getUnits().add(new Unit(playerId, 3, region.getId()));

        assertThat(resolver.canResolve(game, playerId)).isFalse();
    }


    @Test
    void shouldRejectResolvingTwiceInSameActivation() {
        var players = List.of(
                playerFactory.create("A", Hero.ULRIKE)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();


        RegionState region = game.getRegionByNumber(1);
        region.getFeatures().add(RegionFeature.IN_GAME);
        region.getUnits().add(new Unit(playerId, 3, region.getId()));

        resolver.resolve(game, new MakerSpyDecision(
                playerId,
                AbilitiesType.ATTACK,
                AbilitiesType.SPEED
        ));

        assertThatThrownBy(() ->
                resolver.resolve(game, new MakerSpyDecision(
                        playerId,
                        AbilitiesType.SHIELD,
                        AbilitiesType.DOMINATION
                ))
        ).isInstanceOf(IllegalStateException.class);
    }
}