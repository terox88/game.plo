package com.game.game.application.command;

import com.game.game.domain.ActionFieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlaceActionMarkerOnFieldAction implements GameActionCommand {

    private UUID playerId;
    private ActionFieldType field;
}
