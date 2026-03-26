package com.game.game.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ViperGorge {

    private List<ActionMarker> actionMarkers = new ArrayList<>();

    private List<InfluenceMarker> influenceMarkers = new ArrayList<>();

    public void addActionMarker(ActionMarker marker) {
        actionMarkers.add(marker);
    }

    public void removeActionMarker(ActionMarker marker) {
        actionMarkers.remove(marker);
    }

    public void addInfluenceMarker(InfluenceMarker marker) {
        influenceMarkers.add(marker);
    }
}
