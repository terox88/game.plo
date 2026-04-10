package com.game.game.application.action;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlaceActionMarkerOnViperGorgeAction implements GameAction {
    private UUID playerId;
}
