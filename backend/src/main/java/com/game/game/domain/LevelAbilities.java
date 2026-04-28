package com.game.game.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class LevelAbilities {

    private int level;
    private int globalSlots;
    private int availableSlots;
    private List<AbilitiesType> abilities;
    private int baseAbilitiesCount;

    public LevelAbilities(int level, int globalSlots, int availableSlots, List<AbilitiesType> abilities) {
        this.level = level;
        this.globalSlots = globalSlots;
        this.availableSlots = availableSlots;
        this.abilities = abilities;
        this.baseAbilitiesCount = abilities.size();
    }


    public void addAbility(AbilitiesType ability) {
        if (abilities.size() >= globalSlots) {
            throw new IllegalStateException("No free ability slots for level " + level);
        }
        abilities.add(ability);
    }
    public boolean removeAbility(AbilitiesType ability) {
        if (abilities.contains(ability)) {
            return abilities.remove(ability);
        }else {
            throw new IllegalStateException("No such ability for level " + level);
        }
    }

    public boolean hasAbility(AbilitiesType ability) {
        return abilities.contains(ability);
    }

    public int getBaseAbilitiesCount() {
        return baseAbilitiesCount;
    }
}