package com.game.game.factory;

import com.game.game.domain.*;

import java.util.*;

public class PlayerStateFactory {

    public PlayerState create(String name, Hero hero) {

        return PlayerState.builder()
                .playerId(UUID.randomUUID())
                .name(name)
                .hero(hero)

                .level1(new LevelAbilities(1, 2, new ArrayList<>()))
                .level2(new LevelAbilities(2, 3, new ArrayList<>()))
                .level3(new LevelAbilities(3, 4, new ArrayList<>()))

                .unitLevel1(3)
                .unitLevel2(2)
                .unitLevel3(1)

                .gold(2)
                .population(4)

                .mana0(1)
                .mana1(0)
                .mana2(0)
                .mana3(0)

                .reputation(0)
                .victoryPoints(0)
                .vukoTokens(0)

                .availableActionMarkers(5)
                .availableInfluenceMarkers(20)

                .build();
    }
}