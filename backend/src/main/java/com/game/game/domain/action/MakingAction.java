
package com.game.game.domain.action;
import com.game.game.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

                if (decision.getValue() == MakingChoice.PASS) {
                    markUsed(context, USED_SUMMON);
                    context.getState().put(STEP, MakingStep.CHOOSE_ACTION);
                    return nextStep(context);
                }

                handleSummon(context, decision);
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


    private void handleUpgrade(ActionContext context) {
        markUsed(context, USED_UPGRADE);
    }

    private void handleSummon(ActionContext context, PlayerDecision decision) {

        SummonDecision summon = (SummonDecision) decision.getValue();

        int level = summon.level();
        Map<Integer, Integer> placements = summon.regionToAmount();

        PlayerState player = getPlayer(context);
        GameState game = context.getGame();
        UUID playerId = context.getMarker().getPlayerId();

        int totalUnits = placements.values().stream().mapToInt(i -> i).sum();

        // =========================
        // WALIDACJE
        // =========================

        if (level < 1 || level > 3) {
            throw new IllegalArgumentException("Invalid unit level");
        }

        if (totalUnits <= 0) {
            throw new IllegalArgumentException("Must summon at least 1 unit");
        }

        // dostępność jednostek
        int available = getAvailableUnits(player, level);

        if (totalUnits > available) {
            throw new IllegalStateException("Not enough units available");
        }

        // mana
        int mana = player.getMana(level);

        if (mana < totalUnits) {
            throw new IllegalStateException("Not enough mana");
        }

        // =========================
        // WALIDACJA REGIONÓW
        // =========================

        for (var entry : placements.entrySet()) {

            RegionState region = game.getRegionByNumber(entry.getKey());

            boolean hasInfluence = region.getInfluenceMarkers().stream()
                    .anyMatch(m -> m.getPlayerId().equals(playerId));

            boolean hasUnit = region.getUnits().stream()
                    .anyMatch(u -> u.getOwnerId().equals(playerId));
            if (!region.isActive()) {
                throw new IllegalStateException("Cannot summon to not active region");
            }
            if (!hasInfluence && !hasUnit) {
                throw new IllegalStateException("Cannot summon to region without influence or unit");
            }

        }

        // =========================
        // WYKONANIE
        // =========================

        // mana -
        player.setMana(level, mana - totalUnits);

        // units -
        decreaseUnits(player, level, totalUnits);

        // placement
        for (var entry : placements.entrySet()) {

            RegionState region = game.getRegionByNumber(entry.getKey());

            for (int i = 0; i < entry.getValue(); i++) {
                region.getUnits().add(new Unit(playerId, level, region.getId()));
            }
        }

        // =========================
        // EFEKTY
        // =========================

        game.setDeadSnow(game.getDeadSnow() + 1);

        int reputationDelta = totalUnits * level;

        if (level == 1 && player.isVanDyken()) {
            reputationDelta += 2;
        }

        ReputationService reputationService = new ReputationService();

        reputationService.changeReputation(game, playerId, reputationDelta);

        markUsed(context, USED_SUMMON);
    }

    private int getAvailableUnits(PlayerState player, int level) {
        return switch (level) {
            case 1 -> player.getUnitLevel1();
            case 2 -> player.getUnitLevel2();
            case 3 -> player.getUnitLevel3();
            default -> throw new IllegalArgumentException();
        };
    }

    private void decreaseUnits(PlayerState player, int level, int amount) {
        switch (level) {
            case 1 -> player.setUnitLevel1(player.getUnitLevel1() - amount);
            case 2 -> player.setUnitLevel2(player.getUnitLevel2() - amount);
            case 3 -> player.setUnitLevel3(player.getUnitLevel3() - amount);
            default -> throw new IllegalArgumentException();
        }
    }
}

