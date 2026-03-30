package com.game.game.factory;

import com.game.game.domain.*;

import java.util.*;

public class LandTokenFactory {

    public List<RegionToken> create(int playersCount) {
        List<RegionToken> tokens = new ArrayList<>();

        int limit = playersCount + 5;

        for (int i = 1; i < limit; i++) {
            tokens.add(
                    RegionToken.builder()
                            .id(UUID.randomUUID())
                            .orderNumber(i)
                            .prizes(createPrizes(playersCount, i))
                            .placementPoints(createPoints(playersCount, i))
                            .build()
            );
        }

        return tokens;
    }

    private List<Prize> createPrizes(int players, int order) {
        return List.of(
                new Prize(mana(players, order), PriceType.MANA),
                new Prize(gold(players, order), PriceType.GOLD),
                new Prize(population(players, order), PriceType.POPULATION)
        );
    }

    private Map<Integer, Integer> createPoints(int players, int order) {
        Map<Integer, Integer> map = new HashMap<>();

        int first = winner(players, order);
        int second = second(players, order);
        int third = third(players, order);

        if (first > 0) map.put(1, first);
        if (second > 0) map.put(2, second);
        if (third > 0) map.put(3, third);

        return map;
    }

    private int mana(int p, int o) {
        if (p == 2) return (o == 3 || o == 4) ? 1 : 0;
        if (p == 3) return (o == 2 || o == 3 || o == 5) ? 1 : 0;
        return (o == 2 || o == 3 || o == 4 || o == 6) ? 1 : 0;
    }

    private int gold(int p, int o) {
        if (p == 2) return o == 5 ? 2 : 1;
        if (p == 3) return (o >= 5) ? 2 : 1;
        return (o >= 6) ? 2 : 1;
    }

    private int population(int p, int o) {
        if (p == 2) return o == 6 ? 2 : 1;
        if (p == 3) return (o == 4 || o >= 6) ? 2 : 1;
        return (o == 5 || o >= 7) ? 2 : 1;
    }

    private int winner(int p, int o) {
        if (p == 2) return (o <= 3) ? 5 : (o <= 5 ? 4 : 3);
        if (p == 3) return o == 1 ? 6 : (o <= 4 ? 5 : 4);
        return (o <= 3) ? 6 : (o <= 6 ? 5 : 4);
    }

    private int second(int p, int o) {
        if (p == 2) return o == 1 ? 4 : (o <= 3 ? 3 : 2);
        if (p == 3) return o == 1 ? 4 : (o <= 4 ? 3 : 2);
        return (o <= 3) ? 4 : (o <= 6 ? 3 : 2);
    }

    private int third(int p, int o) {
        if (p == 2) return -1;
        if (p == 3) return o == 1 ? 3 : (o <= 4 ? 2 : 1);
        return (o <= 3) ? 3 : (o <= 6 ? 2 : 1);
    }
}