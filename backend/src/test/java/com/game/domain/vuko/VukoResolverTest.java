package com.game.domain.vuko;

import com.game.game.domain.*;
import com.game.game.domain.service.ReputationService;
import com.game.game.domain.vuko.*;
import com.game.game.factory.GameSetupFactory;
import com.game.game.factory.PlayerStateFactory;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class VukoResolverTest {

    private final PlayerStateFactory playerFactory = new PlayerStateFactory();
    private final GameSetupFactory gameFactory = new GameSetupFactory();

    private final VukoResolver resolver = new VukoResolver();

    @Test
    void shouldMoveVukoToRegionWhereWorstPlayerHasMostInfluence() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID worstPlayerId = prepareWorstPlayer(
                game,
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId()
        );

        RegionState r1 = activeRegion(game, 1);
        RegionState r2 = activeRegion(game, 2);

        addInfluence(game, worstPlayerId, r1, 1);
        addInfluence(game, worstPlayerId, r2, 2);

        resolver.start(game);

        assertThat(game.getVukoRegionId()).isEqualTo(r2.getId());
        assertThat(game.findPlayer(worstPlayerId).getVukoTokens()).isEqualTo(1);
        assertThat(game.getCurrentPhase()).isEqualTo(Phase.DOMINATION);
        assertThat(game.getVukoStep()).isEqualTo(VukoStep.END);
    }

    @Test
    void shouldUseLandTokenOrderNumberAsTieBreaker() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID worstPlayerId = prepareWorstPlayer(
                game,
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId()
        );

        RegionState r1 = activeRegion(game, 1);
        RegionState r2 = activeRegion(game, 2);

        List<RegionToken> tokens = game.getAvailableTokens().stream()
                .sorted(Comparator.comparingInt(RegionToken::getOrderNumber))
                .toList();

        RegionToken tokenLow = tokens.get(0);
        RegionToken tokenHigh = tokens.get(1);

        r1.setLandToken(tokenHigh);
        r2.setLandToken(tokenLow);

        addInfluence(game, worstPlayerId, r1, 1);
        addInfluence(game, worstPlayerId, r2, 1);

        resolver.start(game);

        assertThat(game.getVukoRegionId()).isEqualTo(r2.getId());
    }

    @Test
    void shouldMoveToNextBestRegionIfVukoAlreadyInBestRegion() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID worstPlayerId = prepareWorstPlayer(
                game,
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId()
        );

        RegionState best = activeRegion(game, 1);
        RegionState second = activeRegion(game, 2);

        addInfluence(game, worstPlayerId, best, 3);
        addInfluence(game, worstPlayerId, second, 1);

        game.setVukoRegionId(best.getId());

        resolver.start(game);

        assertThat(game.getVukoRegionId()).isEqualTo(second.getId());
    }

    @Test
    void shouldNotMoveIfOnlyInfluenceRegionAlreadyHasVuko() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID worstPlayerId = prepareWorstPlayer(
                game,
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId()
        );

        RegionState onlyRegion = activeRegion(game, 1);

        addInfluence(game, worstPlayerId, onlyRegion, 2);

        game.setVukoRegionId(onlyRegion.getId());

        resolver.start(game);

        assertThat(game.getVukoRegionId()).isEqualTo(onlyRegion.getId());
        assertThat(game.findPlayer(worstPlayerId).getVukoTokens()).isEqualTo(1);
    }

    @Test
    void shouldIgnoreClosedRegion() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID worstPlayerId = prepareWorstPlayer(
                game,
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId()
        );

        RegionState closed = activeRegion(game, 1);
        RegionState open = activeRegion(game, 2);

        closed.getFeatures().add(RegionFeature.CLOSED);

        addInfluence(game, worstPlayerId, closed, 3);
        addInfluence(game, worstPlayerId, open, 1);

        resolver.start(game);

        assertThat(game.getVukoRegionId()).isEqualTo(open.getId());
    }

    @Test
    void shouldAutoKillSingleUnitAndFinish() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID worstPlayerId = prepareWorstPlayer(
                game,
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId()
        );

        RegionState region = activeRegion(game, 1);
        addInfluence(game, worstPlayerId, region, 1);

        Unit unit = new Unit(worstPlayerId, 1, region.getId());
        region.getUnits().add(unit);

        resolver.start(game);

        assertThat(unit.isKilled()).isTrue();
        assertThat(game.getVukoStep()).isEqualTo(VukoStep.END);
        assertThat(game.getCurrentPlayerId()).isNull();
        assertThat(game.getCurrentPhase()).isEqualTo(Phase.DOMINATION);
    }

    @Test
    void shouldWaitForKillDecisionWhenMoreThanOneUnit() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID worstPlayerId = prepareWorstPlayer(
                game,
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId()
        );

        RegionState region = activeRegion(game, 1);
        addInfluence(game, worstPlayerId, region, 1);

        Unit unitA = new Unit(worstPlayerId, 1, region.getId());
        Unit unitB = new Unit(worstPlayerId, 2, region.getId());

        region.getUnits().add(unitA);
        region.getUnits().add(unitB);

        resolver.start(game);

        assertThat(game.getVukoStep()).isEqualTo(VukoStep.WAITING_FOR_KILL);
        assertThat(game.getCurrentPlayerId()).isEqualTo(worstPlayerId);
        assertThat(unitA.isKilled()).isFalse();
        assertThat(unitB.isKilled()).isFalse();
    }

    @Test
    void shouldKillSelectedUnitAndFinish() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID worstPlayerId = prepareWorstPlayer(
                game,
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId()
        );

        RegionState region = activeRegion(game, 1);
        addInfluence(game, worstPlayerId, region, 1);

        Unit unitA = new Unit(worstPlayerId, 1, region.getId());
        Unit unitB = new Unit(worstPlayerId, 2, region.getId());

        region.getUnits().add(unitA);
        region.getUnits().add(unitB);

        resolver.start(game);

        resolver.handleKillDecision(game, new VukoKillDecision(
                worstPlayerId,
                unitB.getId()
        ));

        assertThat(unitA.isKilled()).isFalse();
        assertThat(unitB.isKilled()).isTrue();

        assertThat(game.getVukoStep()).isEqualTo(VukoStep.END);
        assertThat(game.getCurrentPlayerId()).isNull();
        assertThat(game.getCurrentPhase()).isEqualTo(Phase.DOMINATION);
    }

    @Test
    void shouldRejectKillDecisionWhenNotWaitingForKill() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerId = players.get(0).getPlayerId();

        game.setVukoStep(VukoStep.END);

        assertThatThrownBy(() ->
                resolver.handleKillDecision(game, new VukoKillDecision(
                        playerId,
                        UUID.randomUUID()
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRejectKillDecisionFromWrongPlayer() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        UUID playerA = players.get(0).getPlayerId();
        UUID playerB = players.get(1).getPlayerId();

        UUID worstPlayerId = prepareWorstPlayer(game, playerA, playerB);
        UUID wrongPlayerId = worstPlayerId.equals(playerA) ? playerB : playerA;

        RegionState region = activeRegion(game, 1);
        addInfluence(game, worstPlayerId, region, 1);

        Unit unitA = new Unit(worstPlayerId, 1, region.getId());
        Unit unitB = new Unit(worstPlayerId, 2, region.getId());

        region.getUnits().add(unitA);
        region.getUnits().add(unitB);

        resolver.start(game);

        assertThatThrownBy(() ->
                resolver.handleKillDecision(game, new VukoKillDecision(
                        wrongPlayerId,
                        unitA.getId()
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenWorstPlayerHasNoInfluenceOnBoard() {
        var players = List.of(
                playerFactory.create("A", Hero.OLAF),
                playerFactory.create("B", Hero.PIER)
        );

        GameState game = gameFactory.createGame(players);

        prepareWorstPlayer(
                game,
                players.get(0).getPlayerId(),
                players.get(1).getPlayerId()
        );

        assertThatThrownBy(() -> resolver.start(game))
                .isInstanceOf(IllegalStateException.class);
    }

    private UUID prepareWorstPlayer(GameState game, UUID playerA, UUID playerB) {
        makeWorstByReputation(game, playerA, playerB);
        return new ReputationService().getWorstPlayer(game);
    }

    private void makeWorstByReputation(GameState game, UUID worstPlayerId, UUID otherPlayerId) {
        game.findPlayer(worstPlayerId).setReputation(1);
        game.findPlayer(otherPlayerId).setReputation(2);

        game.getReputationTrack().getSlots().forEach(slot -> {
            slot.getPlayers().remove(worstPlayerId);
            slot.getPlayers().remove(otherPlayerId);
        });

        game.getReputationTrack().getSlots().get(1).addOnTop(worstPlayerId);
        game.getReputationTrack().getSlots().get(2).addOnTop(otherPlayerId);
    }

    private RegionState activeRegion(GameState game, int number) {
        RegionState region = game.getRegionByNumber(number);
        region.getFeatures().add(RegionFeature.IN_GAME);
        return region;
    }

    private void addInfluence(GameState game, UUID playerId, RegionState region, int amount) {
        for (int i = 0; i < amount; i++) {
            region.getInfluenceMarkers().add(new InfluenceMarker(playerId));
        }
    }
}