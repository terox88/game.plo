package com.game.game.domain.action;

import com.game.game.domain.*;
import com.game.game.domain.service.MovementService;

import java.util.List;
import java.util.UUID;

public class MoveAction implements GameActionDomain {

    private final MovementService movementService = new MovementService();

    @Override
    public ActionResult start(ActionContext context) {
        return ActionResult.decision(List.of("MOVE"));
    }

    @Override
    public ActionResult handleDecision(ActionContext context, PlayerDecision decision) {

        if (decision.getValue() == MakingChoice.PASS) {
            return ActionResult.finished();
        }

        MoveDecision moveDecision = (MoveDecision) decision.getValue();

        GameState game = context.getGame();
        UUID playerId = context.getMarker().getPlayerId();
        PlayerState player = game.findPlayer(playerId);

        List<SingleMove> moves = moveDecision.moves();

        if (moves.isEmpty() || moves.size() > 2) {
            throw new IllegalStateException("Invalid number of moves");
        }

        if (player.getGold() < moves.size()) {
            throw new IllegalStateException("Not enough gold");
        }

        for (SingleMove move : moves) {

            boolean isUnitMove = move.unitId() != null;
            boolean isInfluenceMove = move.influenceOwnerId() != null;

            if (isUnitMove == isInfluenceMove) {
                throw new IllegalStateException("Must move either unit or influence");
            }

            if (isUnitMove) {
                movementService.moveUnit(
                        game,
                        playerId,
                        move.fromRegionId(),
                        move.toRegionId(),
                        move.unitId(),
                        true
                );
            }

            if (isInfluenceMove) {
                movementService.moveInfluence(
                        game,
                        playerId,
                        move.fromRegionId(),
                        move.toRegionId(),
                        true
                );
            }
        }

        player.spendGold(moves.size());

        return ActionResult.finished();
    }

    @Override
    public boolean isFinished(ActionContext context) {
        return true;
    }
}