package com.game.game.application.command;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AssignTokenToRegionAction implements GameActionCommand {

    private UUID playerId;

    private UUID regionId;

    private UUID tokenId;
}