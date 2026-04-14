package com.game.game.domain.action;

import java.util.List;

public class ActionResult {

    private final boolean requiresDecision;
    private final List<Object> options;

    private ActionResult(boolean requiresDecision, List<Object> options) {
        this.requiresDecision = requiresDecision;
        this.options = options;
    }

    public static ActionResult decision(List<Object> options) {
        return new ActionResult(true, options);
    }

    public static ActionResult finished() {
        return new ActionResult(false, List.of());
    }

    public boolean requiresDecision() {
        return requiresDecision;
    }

    public List<Object> getOptions() {
        return options;
    }

    public static class MoveAction implements GameActionDomain {
        @Override
        public ActionResult start(ActionContext context) {
            return null;
        }

        @Override
        public ActionResult handleDecision(ActionContext context, PlayerDecision decision) {
            return null;
        }

        @Override
        public boolean isFinished(ActionContext context) {
            return false;
        }
    }
}
