package com.game.game.domain;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionToken {

    private UUID id;

    private int orderNumber;

    private List<Prize> prizes;

    // miejsce -> punkty
    private Map<Integer, Integer> placementPoints;
}