package com.game.game.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerState {

    private UUID playerId;
    private String name;
    private Hero hero;

    @Builder.Default
    private LevelAbilities level1 = new LevelAbilities(1, 2, new ArrayList<>());

    @Builder.Default
    private LevelAbilities level2 = new LevelAbilities(2, 3, new ArrayList<>());

    @Builder.Default
    private LevelAbilities level3 = new LevelAbilities(3, 4, new ArrayList<>());

    private int unitLevel1;
    private int unitLevel2;
    private int unitLevel3;

    private int gold;
    private int population;

    private int mana0;
    private int mana1;
    private int mana2;
    private int mana3;

    private int reputation;
    private int victoryPoints;
    private int vukoTokens;

    private int availableActionMarkers;
    private int availableInfluenceMarkers;

    public static PlayerState create(String name, Hero hero) {
        return PlayerState.builder()
                .playerId(UUID.randomUUID())
                .name(name)
                .hero(hero)
                .build();
    }

    public boolean hasAbility(int level, AbilitiesType ability) {
        return switch (level) {
            case 1 -> level1.hasAbility(ability);
            case 2 -> level2.hasAbility(ability);
            case 3 -> level3.hasAbility(ability);
            default -> false;
        };
    }
}