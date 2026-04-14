package com.game.game.domain;

import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    private UUID id;

    private UUID ownerId;

    private int level;

    private UUID regionId;

    public Unit(UUID ownerId, int level, UUID regionId) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.level = level;
        this.regionId = regionId;
    }
}