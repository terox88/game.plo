package com.game.game.application.action;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdvanceInitiativeAction {

    private UUID playerId;
    private int steps;
}
