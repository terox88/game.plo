package com.game.game.domain.service;
import com.game.game.domain.GameState;
import com.game.game.domain.PlayerState;
import com.game.game.domain.ReputationSlot;
import com.game.game.domain.ReputationTrack;

import java.util.Deque;
import java.util.UUID;


public class ReputationService {

    public void changeReputation(GameState game, UUID playerId, int delta) {

        PlayerState player = game.findPlayer(playerId);

        int maxLevel = 10;
        int oldLevel = player.getReputation();
        int newLevel = oldLevel + delta;
        if (newLevel > maxLevel) {
            newLevel = maxLevel;
        }

        validateReputationChange(oldLevel, newLevel);

        if (delta == 0) {
            return;
        }

        ReputationTrack track = game.getReputationTrack();
        track.getSlot(oldLevel).remove(playerId);

        if (delta > 0) {
            track.getSlot(newLevel).addOnTop(playerId);
        } else {
            track.getSlot(newLevel).addAtBottom(playerId);
        }

        player.setReputation(newLevel);
    }

    private void validateReputationChange(int oldLevel, int newLevel) {


        if (newLevel == 0 && oldLevel != 0) {
            throw new IllegalStateException("Cannot return to Yin-Yang");
        }

        if (newLevel < 0) {
            throw new IllegalStateException("Invalid reputation level");
        }
    }

    public UUID getWorstPlayer(GameState game) {

        return game.getReputationTrack().getSlots().stream()
                .sorted((a, b) -> Integer.compare(b.getLevel(), a.getLevel()))
                .map(ReputationSlot::getPlayers)
                .filter(deque -> !deque.isEmpty())
                .map(Deque::peekFirst)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No players on track"));
    }

    public int comparePlayers(GameState game, UUID p1, UUID p2) {

        PlayerState player1 = game.findPlayer(p1);
        PlayerState player2 = game.findPlayer(p2);

        int level1 = player1.getReputation();
        int level2 = player2.getReputation();

        // 🔴 wyższy level = gorszy
        if (level1 != level2) {
            return Integer.compare(level1, level2);
        }

        // 🔥 TEN SAM SLOT → sprawdzamy kolejność w deque
        Deque<UUID> players = game.getReputationTrack().getSlot(level1).getPlayers();

        int index1 = indexOf(players, p1);
        int index2 = indexOf(players, p2);

        return Integer.compare(index2, index1);
        // 🔥 mniejszy index = bardziej z przodu = gorszy
    }

    private int indexOf(Deque<UUID> deque, UUID playerId) {
        int i = 0;
        for (UUID id : deque) {
            if (id.equals(playerId)) return i;
            i++;
        }
        throw new IllegalStateException("Player not found in slot");
    }

    public void improveReputationWithoutReturningToZero(
            GameState game,
            UUID playerId,
            int amount
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        PlayerState player = game.findPlayer(playerId);

        int oldLevel = player.getReputation();

        /*
         * Shadow Rave:
         * nie można wrócić na YinYang (0),
         * więc minimalny legalny poziom to 1
         */
        int targetLevel = Math.max(1, oldLevel - amount);

        /*
         * jeśli już jesteśmy na 0 lub 1
         * i nic realnie się nie zmienia
         */
        if (oldLevel == targetLevel) {
            return;
        }

        ReputationTrack track = game.getReputationTrack();

        // usuń ze starego slotu
        track.getSlot(oldLevel).remove(playerId);

        /*
         * poprawa reputacji zawsze trafia
         * na dół stosu
         */
        track.getSlot(targetLevel).addAtBottom(playerId);

        player.setReputation(targetLevel);
    }

}
