package com.game.game.domain.action.factory;


import com.game.game.domain.ActionFieldType;
import com.game.game.domain.ViperGorge;
import com.game.game.domain.action.*;

public class DomainActionFactory {

    public GameActionDomain get(ActionFieldType type) {
        return switch (type) {
            case MAKING -> new MakingAction();
            case MOVE -> new MoveAction();
            case INFLUENCES -> new InfluenceAction();
            case MOUNTAIN -> new MountainAction();
            case SHADOW_RAVE -> new ShadowRaveAction();
            case VIPER_GORGE -> new ViperGorgeAction();
        };
    }
}
