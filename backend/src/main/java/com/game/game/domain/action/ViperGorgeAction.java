package com.game.game.domain.action;

public class ViperGorgeAction implements GameActionDomain {
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
