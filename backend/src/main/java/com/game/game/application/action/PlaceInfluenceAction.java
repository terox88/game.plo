package com.game.game.application.action;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlaceInfluenceAction {

    private UUID playerId;
    private UUID regionId;
}