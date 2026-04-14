package com.game.game.domain.action;

public interface GameActionDomain {

    ActionResult start(ActionContext context);

    ActionResult handleDecision(ActionContext context, PlayerDecision decision);

    boolean isFinished(ActionContext context);
}
