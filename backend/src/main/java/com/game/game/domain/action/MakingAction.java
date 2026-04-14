
package com.game.game.domain.action;
import com.game.game.domain.GameState;
import com.game.game.domain.PlayerState;
import com.game.game.domain.ReputationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MakingAction implements GameActionDomain {

    private final ReputationService reputationService = new ReputationService();
    private static final String USED_MOVE = "usedMove";
    private static final String USED_UPGRADE = "usedUpgrade";
    private static final String USED_SUMMON = "usedSummon";

    private static final String STEP = "step";
    private static final String MOVE_POINTS = "movePoints";

    private enum MakingStep {
        CHOOSE_ACTION,
        MOVE_EXECUTE,
        UPGRADE_SELECT,
        SUMMON_SELECT
    }

    @Override
    public ActionResult start(ActionContext context) {

        context.getState().put(USED_MOVE, false);
        context.getState().put(USED_UPGRADE, false);
        context.getState().put(USED_SUMMON, false);

        context.getState().put(STEP, MakingStep.CHOOSE_ACTION);

        return nextStep(context);
    }

    @Override
    public ActionResult handleDecision(ActionContext context, PlayerDecision decision) {

        MakingStep step = (MakingStep) context.getState().get(STEP);

        switch (step) {

            case CHOOSE_ACTION -> {
                MakingChoice choice = (MakingChoice) decision.getValue();

                switch (choice) {

                    case MOVE -> {
                        context.getState().put(STEP, MakingStep.MOVE_EXECUTE);
                        context.getState().put(MOVE_POINTS, 5);

                        applyMoveStartEffects(context);

                        return ActionResult.decision(List.of("MOVE_MANA"));
                    }

                    case UPGRADE -> {
                        context.getState().put(STEP, MakingStep.UPGRADE_SELECT);
                        return ActionResult.decision(List.of("UPGRADE"));
                    }

                    case SUMMON -> {
                        context.getState().put(STEP, MakingStep.SUMMON_SELECT);
                        return ActionResult.decision(List.of("SUMMON"));
                    }

                    case PASS -> {
                        return ActionResult.finished();
                    }
                }
            }

            case MOVE_EXECUTE -> {

                if (decision.getValue() == MakingChoice.PASS) {
                    markUsed(context, USED_MOVE);
                    context.getState().put(STEP, MakingStep.CHOOSE_ACTION);
                    return nextStep(context);
                }

                MoveManaDecision move = (MoveManaDecision) decision.getValue();

                int from = move.from();
                int to = move.to();
                int amount = move.amount();

                if (from < 0 || from > 3 || to < 0 || to > 3) {
                    throw new IllegalArgumentException("Invalid mana level");
                }

                if (amount <= 0) {
                    throw new IllegalArgumentException("Amount must be > 0");
                }

                if (from == to) {
                    throw new IllegalArgumentException("Source and target cannot be the same");
                }

                int costPerUnit = Math.abs(from - to);
                int totalCost = costPerUnit * amount;

                int remaining = (int) context.getState().get(MOVE_POINTS);

                if (totalCost > remaining) {
                    throw new IllegalStateException("Not enough move points");
                }

                PlayerState player = getPlayer(context);

                moveMana(player, from, to, amount);

                remaining -= totalCost;
                context.getState().put(MOVE_POINTS, remaining);

                if (remaining <= 0) {
                    markUsed(context, USED_MOVE);
                    context.getState().put(STEP, MakingStep.CHOOSE_ACTION);
                    return nextStep(context);
                }

                return ActionResult.decision(List.of(
                        "MOVE_MANA",
                        MakingChoice.PASS
                ));
            }

            case UPGRADE_SELECT -> {
                handleUpgrade(context);
                context.getState().put(STEP, MakingStep.CHOOSE_ACTION);
                return nextStep(context);
            }

            case SUMMON_SELECT -> {
                handleSummon(context);
                context.getState().put(STEP, MakingStep.CHOOSE_ACTION);
                return nextStep(context);
            }
        }

        throw new IllegalStateException("Invalid step");
    }

    @Override
    public boolean isFinished(ActionContext context) {
        return allUsed(context);
    }

    // =========================

    private ActionResult nextStep(ActionContext context) {

        List<Object> options = new ArrayList<>();

        if (!used(context, USED_MOVE)) options.add(MakingChoice.MOVE);
        if (!used(context, USED_UPGRADE)) options.add(MakingChoice.UPGRADE);
        if (!used(context, USED_SUMMON)) options.add(MakingChoice.SUMMON);

        options.add(MakingChoice.PASS);

        if (options.size() == 1 && options.contains(MakingChoice.PASS)) {
            return ActionResult.finished();
        }

        return ActionResult.decision(options);
    }

    private boolean allUsed(ActionContext context) {
        return used(context, USED_MOVE)
                && used(context, USED_UPGRADE)
                && used(context, USED_SUMMON);
    }

    private boolean used(ActionContext context, String key) {
        return (boolean) context.getState().getOrDefault(key, false);
    }

    private void markUsed(ActionContext context, String key) {
        context.getState().put(key, true);
    }

    // =========================
    // MOVE LOGIC
    // =========================

    private void applyMoveStartEffects(ActionContext context) {

        GameState game = context.getGame();

        reputationService.changeReputation(
                context.getGame(),
                context.getMarker().getPlayerId(),
                +1
        );
        game.setDeadSnow(game.getDeadSnow() + 1);
    }


    private void moveMana(PlayerState player, int from, int to, int amount) {

        int fromValue = getMana(player, from);

        if (fromValue < amount) {
            throw new IllegalStateException("Not enough mana on source");
        }

        setMana(player, from, fromValue - amount);

        int toValue = getMana(player, to);
        setMana(player, to, toValue + amount);
    }

    private int getMana(PlayerState p, int level) {
        return p.getMana(level);
    }

    private void setMana(PlayerState p, int level, int value) {
       p.setMana(level, value);
    }

    private PlayerState getPlayer(ActionContext context) {
        UUID playerId = context.getMarker().getPlayerId();
        return context.getGame().findPlayer(playerId);
    }

    // =========================
    // STUBS
    // =========================

    private void handleUpgrade(ActionContext context) {
        markUsed(context, USED_UPGRADE);
    }

    private void handleSummon(ActionContext context) {
        markUsed(context, USED_SUMMON);
    }
}

