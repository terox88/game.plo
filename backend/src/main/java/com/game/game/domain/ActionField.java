package com.game.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ActionField {

    private ActionFieldType type;

    private int capacity;

    private Deque<ActionMarker> markers = new ArrayDeque<>();

    public ActionField(ActionFieldType type, int capacity) {
        this.type = type;
        this.capacity = capacity;
    }

    // 🔵 PLANNING
    public void placeMarker(ActionMarker marker) {
        if (markers.size() >= capacity) {
            throw new IllegalStateException("No free slots on action field");
        }

        markers.addFirst(marker); // 🔥 ostatni → na górze
    }

    public ActionMarker pollNext() {
        return markers.pollFirst(); // 🔥 pierwszy wykonuje
    }

    public boolean isEmpty() {
        return markers.isEmpty();
    }
}
