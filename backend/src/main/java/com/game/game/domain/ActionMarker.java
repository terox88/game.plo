package com.game.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ActionMarker {
    private UUID playerId;
    @Setter
    private ActionFieldType fieldType;
}
