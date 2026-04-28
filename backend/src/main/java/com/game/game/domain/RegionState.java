package com.game.game.domain;

import lombok.*;

import java.util.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionState {

    private UUID id;

    private int number;

    private List<Integer> neighbors;
@Setter
    private RegionToken landToken;

    @Builder.Default
    private List<Uroczysko> uroczyska = new ArrayList<>();
@Setter
    private int neutralMarkerCount;

    @Builder.Default
    private List<InfluenceMarker> influenceMarkers = new ArrayList<>();

    @Builder.Default
    private List<Unit> units = new ArrayList<>();

    @Builder.Default
    private Set<RegionFeature> features = new HashSet<>();

    private int nightmareCount;

    public boolean hasThorn () {
        return features.contains(RegionFeature.THORN);
    }
    public boolean isActive() {
        return features.contains(RegionFeature.IN_GAME);
    }
    public boolean hasTower() {
        return features.contains(RegionFeature.TOWER);
    }
    public boolean hasVuko() {
        return features.contains(RegionFeature.VUKO);
    }
    public boolean isClosed() {
        return features.contains(RegionFeature.CLOSED);
    }
}
