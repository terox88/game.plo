package com.game.game.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlaceInfluenceAction implements GameActionCommand {

    private UUID playerId;
    private UUID regionId;
}