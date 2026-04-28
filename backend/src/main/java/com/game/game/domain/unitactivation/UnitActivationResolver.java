package com.game.game.domain.unitactivation;

import com.game.game.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnitActivationResolver {

    private final MakerSpyResolver makerSpyResolver = new MakerSpyResolver();
    private final DrakkarResolver drakkarResolver = new DrakkarResolver();
    private final SpeedResolver speedResolver = new SpeedResolver();
    private final AttackResolver attackResolver = new AttackResolver();

    public void start(GameState game) {
        game.setCurrentPhase(Phase.UNIT_ACTIVATION);
        game.setUnitActivationStep(UnitActivationStep.MAKER_SPY);
        game.setCurrentUnitActivationPlayerIndex(0);

        // snapshot kolejności dla tej fazy
        game.setCurrentTurnOrder(new ArrayList<>(game.getInitiativeTurnOrder()));

        setCurrentPlayer(game);
        skipUnavailablePlayers(game);
    }

    public void handleMakerSpyDecision(GameState game, MakerSpyDecision decision) {
        validateStep(game, UnitActivationStep.MAKER_SPY);
        validateCurrentPlayer(game, decision.playerId());

        makerSpyResolver.resolve(game, decision);

        advanceToNextPlayerOrStep(game);
    }

    public void skipMakerSpy(GameState game, UUID playerId) {
        validateStep(game, UnitActivationStep.MAKER_SPY);
        validateCurrentPlayer(game, playerId);

        advanceToNextPlayerOrStep(game);
    }

    public void handleDrakkarDecision(GameState game, DrakkarDecision decision) {
        validateStep(game, UnitActivationStep.DRAKAR);
        validateCurrentPlayer(game, decision.playerId());

        drakkarResolver.resolve(game, decision);

        advanceToNextPlayerOrStep(game);
    }

    public void skipDrakkar(GameState game, UUID playerId) {
        validateStep(game, UnitActivationStep.DRAKAR);
        validateCurrentPlayer(game, playerId);

        advanceToNextPlayerOrStep(game);
    }

    public void handleSpeedDecision(GameState game, SpeedDecision decision) {
        validateStep(game, UnitActivationStep.SPEED);
        validateCurrentPlayer(game, decision.playerId());

        speedResolver.resolve(game, decision);

        advanceToNextPlayerOrStep(game);
    }

    public void skipSpeed(GameState game, UUID playerId) {
        validateStep(game, UnitActivationStep.SPEED);
        validateCurrentPlayer(game, playerId);

        advanceToNextPlayerOrStep(game);
    }

    public void handleAttackDecision(GameState game, AttackDecision decision) {
        validateStep(game, UnitActivationStep.ATTACK);
        validateCurrentPlayer(game, decision.playerId());

        attackResolver.resolve(game, decision);

        advanceToNextPlayerOrStep(game);
    }

    public void skipAttack(GameState game, UUID playerId) {
        validateStep(game, UnitActivationStep.ATTACK);
        validateCurrentPlayer(game, playerId);

        advanceToNextPlayerOrStep(game);
    }

    private void advanceToNextPlayerOrStep(GameState game) {
        int nextIndex = game.getCurrentUnitActivationPlayerIndex() + 1;
        game.setCurrentUnitActivationPlayerIndex(nextIndex);

        if (nextIndex >= getTurnOrder(game).size()) {
            advanceToNextStep(game);
            return;
        }

        setCurrentPlayer(game);
        skipUnavailablePlayers(game);
    }

    private void advanceToNextStep(GameState game) {
        switch (game.getUnitActivationStep()) {
            case MAKER_SPY -> game.setUnitActivationStep(UnitActivationStep.DRAKAR);
            case DRAKAR -> game.setUnitActivationStep(UnitActivationStep.SPEED);
            case SPEED -> game.setUnitActivationStep(UnitActivationStep.ATTACK);
            case ATTACK -> game.setUnitActivationStep(UnitActivationStep.CLEANUP);
            case CLEANUP -> game.setUnitActivationStep(UnitActivationStep.END);
            case END -> throw new IllegalStateException("Unit activation already ended");
        }

        game.setCurrentUnitActivationPlayerIndex(0);

        if (game.getUnitActivationStep() == UnitActivationStep.CLEANUP) {
            cleanup(game);
            return;
        }

        if (game.getUnitActivationStep() == UnitActivationStep.END) {
            finish(game);
            return;
        }

        setCurrentPlayer(game);
        skipUnavailablePlayers(game);
    }

    private void cleanup(GameState game) {
        attackResolver.cleanupKilledUnits(game);
        makerSpyResolver.restore(game);

        game.setUnitActivationStep(UnitActivationStep.END);
        finish(game);
    }

    private void finish(GameState game) {
        game.setCurrentUnitActivationPlayerIndex(0);
        game.setCurrentPlayerId(null);
        game.setCurrentTurnOrder(new ArrayList<>());

        game.setCurrentPhase(Phase.VUKO);
    }

    private void skipUnavailablePlayers(GameState game) {
        while (game.getUnitActivationStep() != UnitActivationStep.END
                && game.getUnitActivationStep() != UnitActivationStep.CLEANUP
                && !currentPlayerCanAct(game)) {

            int nextIndex = game.getCurrentUnitActivationPlayerIndex() + 1;
            game.setCurrentUnitActivationPlayerIndex(nextIndex);

            if (nextIndex >= getTurnOrder(game).size()) {
                advanceToNextStep(game);
                return;
            }

            setCurrentPlayer(game);
        }
    }

    private boolean currentPlayerCanAct(GameState game) {
        UUID playerId = getCurrentPlayerId(game);

        return switch (game.getUnitActivationStep()) {
            case MAKER_SPY -> makerSpyResolver.canResolve(game, playerId);
            case DRAKAR -> drakkarResolver.canResolve(game, playerId);
            case SPEED -> speedResolver.hasAnySpeedUnit(game, playerId);
            case ATTACK -> attackResolver.hasAnyAttackUnit(game, playerId)
                    && game.findPlayer(playerId).getGold() >= 2;
            case CLEANUP, END -> false;
        };
    }

    private void setCurrentPlayer(GameState game) {
        game.setCurrentPlayerId(getCurrentPlayerId(game));
    }

    private UUID getCurrentPlayerId(GameState game) {
        List<UUID> order = getTurnOrder(game);

        if (order == null || order.isEmpty()) {
            throw new IllegalStateException("Unit activation turn order is empty");
        }

        int index = game.getCurrentUnitActivationPlayerIndex();

        if (index < 0 || index >= order.size()) {
            throw new IllegalStateException("Invalid unit activation player index");
        }

        return order.get(index);
    }

    private List<UUID> getTurnOrder(GameState game) {
        return game.getCurrentTurnOrder();
    }

    private void validateStep(GameState game, UnitActivationStep expectedStep) {
        if (game.getCurrentPhase() != Phase.UNIT_ACTIVATION) {
            throw new IllegalStateException("Not in UNIT_ACTIVATION phase");
        }

        if (game.getUnitActivationStep() != expectedStep) {
            throw new IllegalStateException(
                    "Invalid unit activation step. Expected "
                            + expectedStep
                            + " but was "
                            + game.getUnitActivationStep()
            );
        }
    }

    private void validateCurrentPlayer(GameState game, UUID playerId) {
        UUID currentPlayerId = game.getCurrentPlayerId();

        if (currentPlayerId == null || !currentPlayerId.equals(playerId)) {
            throw new IllegalStateException("Not current player's unit activation turn");
        }
    }
}