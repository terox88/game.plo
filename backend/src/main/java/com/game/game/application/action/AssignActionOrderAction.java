package com.game.game.application.action;

import com.game.game.domain.ActionFieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AssignActionOrderAction implements GameAction{
    private UUID playerId;
    private ActionFieldType field;
    private int order;
}
