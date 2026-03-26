package com.game.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ActionField {
    private ActionFieldType type;
    private List<ActionMarker> markers = new ArrayList<>();


}
