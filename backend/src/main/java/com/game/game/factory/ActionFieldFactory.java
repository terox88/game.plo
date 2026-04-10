package com.game.game.factory;

import com.game.game.domain.*;

import java.util.*;

public class ActionFieldFactory {

    public List<ActionField> create(int players) {
        return List.of(
                new ActionField(ActionFieldType.INFLUENCES, 8),
                new ActionField(ActionFieldType.MOVE, standardCapacity(players)),
                new ActionField(ActionFieldType.MOUNTAIN, standardCapacity(players)),
                new ActionField(ActionFieldType.SHADOW_RAVE, standardCapacity(players)),
                new ActionField(ActionFieldType.MAKING, standardCapacity(players))
        );
    }

    private int standardCapacity(int players) {
        return players == 2 ? 3 : 4;
    }
}