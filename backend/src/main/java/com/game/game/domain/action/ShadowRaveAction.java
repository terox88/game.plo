package com.game.game.domain.action;

import com.game.game.domain.GameState;
import com.game.game.domain.PlayerState;
import com.game.game.domain.service.ReputationService;

import java.util.List;
import java.util.UUID;

public class ShadowRaveAction implements GameActionDomain {

    private final ReputationService reputationService = new ReputationService();

    @Override
    public ActionResult start(ActionContext context) {
        return ActionResult.decision(List.of(
                ShadowRaveChoice.REDUCE_VUKO,
                ShadowRaveChoice.IMPROVE_REPUTATION,
                MakingChoice.PASS
        ));
    }

    @Override
    public ActionResult handleDecision(ActionContext context, PlayerDecision decision) {

        if (decision.getValue() == MakingChoice.PASS) {
            return ActionResult.finished();
        }

        ShadowRaveChoice choice = (ShadowRaveChoice) decision.getValue();

        GameState game = context.getGame();
        UUID playerId = context.getMarker().getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        switch (choice) {

            case REDUCE_VUKO -> handleReduceVuko(player);

            case IMPROVE_REPUTATION ->
                    reputationService.improveReputationWithoutReturningToZero(
                            game,
                            playerId,
                            2
                    );
        }

        return ActionResult.finished();
    }

    @Override
    public boolean isFinished(ActionContext context) {
        return true;
    }

    private void handleReduceVuko(PlayerState player) {

        if (player.getMana(0) < 1) {
            throw new IllegalStateException("Not enough mana level 0");
        }

        if (player.getVukoTokens() <= 0) {
            throw new IllegalStateException("No Vuko tokens to remove");
        }

        player.spendMana(0, 1);
        player.setVukoTokens(player.getVukoTokens() - 1);
    }
}