package com.game.game.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelAbilities {

    private int level;
    private int globalSlots;
    private int availableSlots;

    @Builder.Default
    private List<AbilitiesType> abilities = new ArrayList<>();


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
        return globalSlots - availableSlots;
    }
}