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
}