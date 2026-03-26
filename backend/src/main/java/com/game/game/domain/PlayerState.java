package com.game.game.domain;

import lombok.*;

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

    // abilities per level
    private LevelAbilities level1;
    private LevelAbilities level2;
    private LevelAbilities level3;

    // units
    private int unitLevel1;
    private int unitLevel2;
    private int unitLevel3;

    // resources
    private int gold;
    private int population;

    private int mana0;
    private int mana1;
    private int mana2;
    private int mana3;

    // game stats
    private int reputation;
    private int victoryPoints;
    private int vukoTokens;
    private int availableActionMarkers;
    private int availableInfluenceMarkers;

    public boolean hasAbility(int level, AbilitiesType ability) {
        return switch (level) {
            case 1 -> level1.hasAbility(ability);
            case 2 -> level2.hasAbility(ability);
            case 3 -> level3.hasAbility(ability);
            default -> false;
        };
    }
}