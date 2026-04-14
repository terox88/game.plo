package com.game.game.domain.action;

import com.game.game.domain.ActionMarker;
import com.game.game.domain.GameState;
import com.game.game.domain.PlayerState;

import java.util.HashMap;
import java.util.Map;

public class ActionContext {

    private final GameState game;
    private final ActionMarker marker;
    private final PlayerState player;

    private final Map<String, Object> state = new HashMap<>();

    public ActionContext(GameState game, ActionMarker marker) {
        this.game = game;
        this.marker = marker;
        this.player = game.findPlayer(marker.getPlayerId());
    }

    public GameState getGame() { return game; }
    public ActionMarker getMarker() { return marker; }
    public PlayerState getPlayer() { return player; }

    public Map<String, Object> getState() { return state; }
}
