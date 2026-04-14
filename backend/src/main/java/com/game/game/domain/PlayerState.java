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
    @Builder.Default
    private int stage = 1;

    @Builder.Default
    private int round = 1;

    @Builder.Default
    private boolean usedDoubleMoveInStage = false;

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

    public void useInfluenceMarker() {

        if (availableInfluenceMarkers <= 0) {
            throw new IllegalStateException("No available influence markers");
        }

        availableInfluenceMarkers--;
    }

    public void retrieveInfluenceMarker() {
        if (availableInfluenceMarkers >= 20) {
            throw new IllegalStateException("All influence markers already available");
        }

        availableInfluenceMarkers++;
    }

    public void useActionMarker() {

        if (availableActionMarkers <= 0) {
            throw new IllegalStateException("No available action markers");
        }

        availableActionMarkers--;
    }
    public void retrieveActionMarker() {

        if (availableActionMarkers >= getActionMarkerLimitForCurrentRound()) {
            throw new IllegalStateException("All action markers already available");
        }

        availableActionMarkers++;
    }

    public void addGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        gold += amount;
    }

    public void spendGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (gold < amount) {
            throw new IllegalStateException("Not enough gold");
        }

        gold -= amount;
    }

    public void addPopulation(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        population += amount;
    }

    public void spendPopulation(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (population < amount) {
            throw new IllegalStateException("Not enough population");
        }

        population -= amount;
    }

    public void spendMana(int level) {

        switch (level) {
            case 0 -> {
                if (mana0 <= 0) throw new IllegalStateException("No mana level 0");
                mana0--;
            }
            case 1 -> {
                if (mana1 <= 0) throw new IllegalStateException("No mana level 1");
                mana1--;
            }
            case 2 -> {
                if (mana2 <= 0) throw new IllegalStateException("No mana level 2");
                mana2--;
            }
            case 3 -> {
                if (mana3 <= 0) throw new IllegalStateException("No mana level 3");
                mana3--;
            }
            default -> throw new IllegalArgumentException("Invalid mana level: " + level);
        }
    }

    public void addMana(int level, int amount) {

        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        switch (level) {
            case 0 -> mana0 += amount;
            case 1 -> mana1 += amount;
            case 2 -> mana2 += amount;
            case 3 -> mana3 += amount;
            default -> throw new IllegalArgumentException("Invalid mana level: " + level);
        }
    }

    public int getMana(int level) {
        return switch (level) {
            case 0 -> mana0;
            case 1 -> mana1;
            case 2 -> mana2;
            case 3 -> mana3;
            default -> throw new IllegalArgumentException("Invalid mana level");
        };
    }

    public void setMana(int level, int value) {
         switch (level) {
            case 0 -> mana0 = value;
            case 1 -> mana1 = value;
            case 2 -> mana2 = value;
            case 3 -> mana3 = value;
            default -> throw new IllegalArgumentException("Invalid mana level");
        };
    }

    public int getActionMarkerLimitForCurrentRound() {

        return switch (stage) {
            case 1, 2 -> switch (round) {
                case 1 -> 3;
                case 2 -> 4;
                case 3 -> 5;
                default -> throw new IllegalStateException("Invalid round for stage " + stage);
            };
            case 3, 4 -> switch (round) {
                case 1 -> 4;
                case 2 -> 5;
                default -> throw new IllegalStateException("Invalid round for stage " + stage);
            };
            default -> throw new IllegalStateException("Invalid stage");
        };
    }

    public void advance(int steps) {

        for (int i = 0; i < steps; i++) {

            if (isLastRoundInStage()) {
                moveToNextStage();
            } else {
                round++;
            }
        }
    }

    private boolean isLastRoundInStage() {
        return switch (stage) {
            case 1, 2 -> round == 3;
            case 3, 4 -> round == 2;
            default -> throw new IllegalStateException("Invalid stage");
        };
    }

    private void moveToNextStage() {

        if (stage == 4) {

            return;
        }

        stage++;
        round = 1;

        usedDoubleMoveInStage = false;
    }

    public void useDoubleMove() {

        if (usedDoubleMoveInStage) {
            throw new IllegalStateException("Double move already used in this stage");
        }

        usedDoubleMoveInStage = true;

        advance(2);
    }

    public void moveOne() {
        advance(1);
    }

    public boolean isVanDyken() {
        return hero == Hero.PIER;
    }

    public void resetActionMarkersForRound() {
        availableActionMarkers = getActionMarkerLimitForCurrentRound();
    }




}