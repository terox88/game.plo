package com.game.game.factory;

import com.game.game.domain.*;

import java.util.*;

public class RegionFactory {

    public List<RegionState> create() {

        List<RegionState> regions = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {
            regions.add(
                    RegionState.builder()
                            .id(UUID.randomUUID())
                            .number(i)
                            .neighbors(neighbors(i))
                            .build()
            );
        }

        return regions;
    }

    private List<Integer> neighbors(int n) {
        return switch (n) {
            case 1 -> List.of(2, 4, 5);
            case 2 -> List.of(1, 3, 5, 6);
            case 3 -> List.of(2, 6, 7);
            case 4 -> List.of(1, 5, 8);
            case 5 -> List.of(1, 2, 4, 6, 8);
            case 6 -> List.of(2, 3, 5, 7, 8);
            case 7 -> List.of(3, 6, 8);
            case 8 -> List.of(4, 5, 6, 7);
            default -> List.of();
        };
    }
}