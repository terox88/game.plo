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
    @Setter
    private UUID regionId;
    private boolean isKilled;
    private boolean hasAttacked;

    public Unit(UUID ownerId, int level, UUID regionId) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.level = level;
        this.regionId = regionId;
    }

    public void kill() {
        this.isKilled = true;
    }

    public void markAttacked() {
        this.hasAttacked = true;
    }

    public void resetAfterUnitActivation() {
        this.isKilled = false;
        this.hasAttacked = false;
    }
}