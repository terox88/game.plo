package com.game.game.domain;

import lombok.*;

import java.util.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameState {

    private List<PlayerState> players;
    private List<RegionState> regions;
    private List<ActionField> actionFields;
    private List<RegionToken> availableTokens;
    private ViperGorge viperGorge;

    private InitiativeTrack initiativeTrack;
    private ReputationTrack reputationTrack;
    private UUID currentPlayerId;

    private Phase currentPhase;
    private List<UUID> currentTurnOrder;

    private List<ActionFieldType> actionResolutionOrder;
    @Builder.Default
    private Map<ActionFieldType, Integer> actionOrderAssignments = new HashMap<>();
    @Builder.Default
    private Set<Integer> usedOrderNumbers = new HashSet<>();
    @Builder.Default
    private Set<ActionFieldType> assignedFields = new HashSet<>();
    @Builder.Default
    private Map<UUID, Integer> placedMarkersInPlanning = new HashMap<>();

    private int deadSnow;
    private int stageLast;
    private int roundLast;
    @Builder.Default
    private Map<UUID,Set<UUID>> setupInfluenceHistory = new HashMap<>();

    public RegionState getRegionByNumber(int number) {
        return regions.stream()
                .filter(r -> r.getNumber() == number)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + number));
    }

    public boolean isVanDykenInGame() {
        return players.stream().anyMatch(PlayerState::isVanDyken);
    }
    public PlayerState findPlayer(UUID playerId) {

        if (playerId == null) {
            throw new IllegalArgumentException("PlayerId cannot be null");
        }

        return players.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }

    public void updateGlobalProgress(PlayerState player) {

        if (player.getStage() > stageLast ||
                (player.getStage() == stageLast && player.getRound() > roundLast)) {

            stageLast = player.getStage();
            roundLast = player.getRound();
        }
    }

    public List<UUID> getTurnOrder() {
        return players.stream()
                .sorted(
                        Comparator
                                .comparingInt(PlayerState::getStage).reversed()
                                .thenComparingInt(PlayerState::getRound)
                                .thenComparingInt(p -> getTrackOrder(p.getPlayerId()))
                )
                .map(PlayerState::getPlayerId)
                .toList();
    }

    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    private int getTrackOrder(UUID playerId) {
        return initiativeTrack.getTurnOrder().indexOf(playerId);
    }

    public List<UUID> calculateTurnOrder() {
        return players.stream()
                .sorted(
                        Comparator
                                .comparingInt(PlayerState::getStage).reversed()
                                .thenComparingInt(PlayerState::getRound)
                                .thenComparingInt(p -> getTrackOrder(p.getPlayerId()))
                )
                .map(PlayerState::getPlayerId)
                .toList();
    }


}