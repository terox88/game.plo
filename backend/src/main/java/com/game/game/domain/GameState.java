package com.game.game.domain;
import com.game.game.domain.action.ActionSubPhase;
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
    private ActionSubPhase actionSubPhase;

    // 🔥 SNAPSHOTY KOLEJNOŚCI
    private List<UUID> currentTurnOrder;      // do rundy
    private List<UUID> initiativeTurnOrder;   // do initiative

    // PLANNING
    private List<ActionFieldType> actionResolutionOrder;

    @Builder.Default
    private Map<ActionFieldType, Integer> actionOrderAssignments = new HashMap<>();

    @Builder.Default
    private Set<Integer> usedOrderNumbers = new HashSet<>();

    @Builder.Default
    private Set<ActionFieldType> assignedFields = new HashSet<>();

    @Builder.Default
    private Map<UUID, Integer> placedMarkersInPlanning = new HashMap<>();

    // GLOBAL PROGRESS
    private int deadSnow;
    private int stageLast;
    private int roundLast;

    @Builder.Default
    private Map<UUID, Set<UUID>> setupInfluenceHistory = new HashMap<>();

    // ACTION STATE
    private int currentFieldIndex;
    private boolean viperGorgeResolved;

    // =========================================================
    // 🔥 NIE USUWAJ TEGO — to było poprawne
    // =========================================================

    public void updateGlobalProgress(PlayerState player) {

        if (player.getStage() > stageLast ||
                (player.getStage() == stageLast && player.getRound() > roundLast)) {

            stageLast = player.getStage();
            roundLast = player.getRound();
        }
    }

    // =========================================================

    public PlayerState findPlayer(UUID playerId) {
        return players.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }

    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public boolean isVanDykenInGame() {
        return players.stream().anyMatch(PlayerState::isVanDyken);
    }

    private int getTrackOrder(UUID playerId) {
        int index = initiativeTrack.getTurnOrder().indexOf(playerId);
        if (index == -1) {
            throw new IllegalStateException("Player not on initiative track");
        }
        return index;
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

    public ActionField getActionField(ActionFieldType type) {
        return actionFields.stream()
                .filter(f -> f.getType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field not found: " + type));
    }

    public RegionState getRegionByNumber(int number) {
        return regions.stream().filter(r -> r.getNumber() == number).findFirst()
                .orElseThrow(()-> new IllegalArgumentException("Region number " + number + "not found"));
    }
}

