package com.game.game.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Getter
@NoArgsConstructor
public class ViperGorge {


    private Deque<ActionMarker> actionMarkers = new ArrayDeque<>();

    private List<InfluenceMarker> influenceMarkers = new ArrayList<>();


    public void addActionMarker(ActionMarker marker) {
        actionMarkers.push(marker); // LIFO
    }

    public ActionMarker popActionMarker() {
        return actionMarkers.pop();
    }

    public boolean hasActionMarkers() {
        return !actionMarkers.isEmpty();
    }


    public void addInfluenceMarker(InfluenceMarker marker) {
        influenceMarkers.add(marker);
    }

    public List<InfluenceMarker> getInfluenceMarkersByPlayer(UUID playerId) {
        return influenceMarkers.stream()
                .filter(m -> m.getPlayerId().equals(playerId))
                .toList();
    }
}