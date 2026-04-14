package com.game.game.domain.action;

import java.util.ArrayList;
import java.util.List;

public class MakingAction implements GameActionDomain {

    private static final String USED_MOVE = "usedMove";
    private static final String USED_UPGRADE = "usedUpgrade";
    private static final String USED_SUMMON = "usedSummon";

    @Override
    public ActionResult start(ActionContext context) {

        context.getState().put(USED_MOVE, false);
        context.getState().put(USED_UPGRADE, false);
        context.getState().put(USED_SUMMON, false);

        return nextStep(context);
    }

    @Override
    public ActionResult handleDecision(ActionContext context, PlayerDecision decision) {

        MakingChoice choice = (MakingChoice) decision.getValue();

        switch (choice) {
            case MOVE -> handleMove(context);
            case UPGRADE -> handleUpgrade(context);
            case SUMMON -> handleSummon(context);
            case PASS -> {
                return ActionResult.finished();
            }
        }

        return nextStep(context);
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
    // 🔥 SUB-ACTIONS (na razie stuby)
    // =========================

    private void handleMove(ActionContext context) {
        markUsed(context, USED_MOVE);

        // TODO:
        // - obniż reputację
        // - podnieś tor śniegu
        // - daj 5 ruchów M
        // - pozwól rozdzielać ruchy
    }

    private void handleUpgrade(ActionContext context) {
        markUsed(context, USED_UPGRADE);

        // TODO:
        // - walidacja zasobów
        // - upgrade jednostki
    }

    private void handleSummon(ActionContext context) {
        markUsed(context, USED_SUMMON);

        // TODO:
        // - koszt reputacji (level * ilość)
        // - placement tylko w regionach z influence
    }
}
