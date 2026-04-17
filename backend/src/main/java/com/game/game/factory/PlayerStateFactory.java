package com.game.game.factory;

import com.game.game.domain.*;

import java.util.*;

public class PlayerStateFactory {

    public PlayerState create(String name, Hero hero) {

        return PlayerState.builder()
                .playerId(UUID.randomUUID())
                .name(name)
                .hero(hero)

                .level1(new LevelAbilities(1, 2, availableSlotsCreator(1,hero), abilitiesCreator(1, hero)))
                .level2(new LevelAbilities(2, 3, availableSlotsCreator(2,hero), abilitiesCreator(2, hero)))
                .level3(new LevelAbilities(3, 4, availableSlotsCreator(3, hero), abilitiesCreator(3, hero)))

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
    public List<AbilitiesType> abilitiesCreator(int level, Hero hero) {

        List<AbilitiesType> levOne = switch (hero) {
            case OLAF, PASSIONARIA, ULRIKE -> new ArrayList<>(List.of(AbilitiesType.ATTACK));
            case PIER -> new ArrayList<>(List.of(AbilitiesType.ATTACK, AbilitiesType.ATTACK));
        };

        List<AbilitiesType> levTwo = switch (hero) {
            case OLAF -> new ArrayList<>(List.of(AbilitiesType.DRAKAR, AbilitiesType.ICE_GARDEN));
            case PASSIONARIA -> new ArrayList<>(List.of(AbilitiesType.FAUN));
            case ULRIKE -> new ArrayList<>(List.of(AbilitiesType.PILLAGE));
            case PIER -> new ArrayList<>(List.of(AbilitiesType.PIPER));
        };

        List<AbilitiesType> levThree = switch (hero) {
            case OLAF -> new ArrayList<>(List.of(AbilitiesType.ATTACK, AbilitiesType.SHIELD));
            case PASSIONARIA -> new ArrayList<>(List.of(AbilitiesType.NIGHTMARE, AbilitiesType.SHIELD));
            case ULRIKE -> new ArrayList<>(List.of(AbilitiesType.MAKER_SPY, AbilitiesType.MAKER_SPY));
            case PIER -> new ArrayList<>(List.of(AbilitiesType.ATTACK, AbilitiesType.SPEED));
        };

        return switch (level) {
            case 1 -> levOne;
            case 2 -> levTwo;
            case 3 -> levThree;
            default -> throw new IllegalArgumentException("Unsupported level: " + level);
        };
    }

    public int availableSlotsCreator(int level, Hero hero) {

        int levOne = switch (hero) {
            case PASSIONARIA, OLAF, ULRIKE -> 1;
            case PIER -> 0;
        };

        int levTwo = switch (hero) {
            case PASSIONARIA, PIER, ULRIKE -> 2;
            case OLAF -> 1;
        };
        return switch (level) {
            case 1 -> levOne;
            case 2 -> levTwo;
            case 3 -> 2;
            default -> throw new IllegalArgumentException("Unsupported level: " + level);
        };

    }
}