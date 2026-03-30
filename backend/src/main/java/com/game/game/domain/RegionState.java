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

    private int neutralMarkerCount;

    @Builder.Default
    private List<InfluenceMarker> influenceMarkers = new ArrayList<>();

    @Builder.Default
    private List<Unit> units = new ArrayList<>();

    @Builder.Default
    private Set<RegionFeature> features = new HashSet<>();

    private int nightmareCount;
}