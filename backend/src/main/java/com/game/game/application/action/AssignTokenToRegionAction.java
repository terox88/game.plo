package com.game.game.application.action;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AssignTokenToRegionAction implements GameAction {

    private UUID playerId;

    private UUID regionId;

    private UUID tokenId;
}