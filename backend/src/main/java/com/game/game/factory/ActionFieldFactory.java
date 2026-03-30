package com.game.game.factory;

import com.game.game.domain.*;

import java.util.*;

public class ActionFieldFactory {

    public List<ActionField> create(int players) {
        return List.of(
                new ActionField(ActionFieldType.INFLUENCES, new ArrayList<>()),
                new ActionField(ActionFieldType.MOVE, new ArrayList<>()),
                new ActionField(ActionFieldType.MOUNTAIN, new ArrayList<>()),
                new ActionField(ActionFieldType.SHADOW_RAVE, new ArrayList<>()),
                new ActionField(ActionFieldType.MAKING, new ArrayList<>())
        );
    }
}