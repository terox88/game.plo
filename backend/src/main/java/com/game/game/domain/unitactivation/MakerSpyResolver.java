package com.game.game.domain.unitactivation;

import com.game.game.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MakerSpyResolver {

    private static final Set<AbilitiesType> ALLOWED_MAKER_SPY_ABILITIES = Set.of(
            AbilitiesType.ATTACK,
            AbilitiesType.SHIELD,
            AbilitiesType.SPEED,
            AbilitiesType.DOMINATION
    );

    public boolean canResolve(GameState game, UUID playerId) {
        PlayerState player = game.findPlayer(playerId);

        if (player.getHero() != Hero.ULRIKE) {
            return false;
        }

        if (!player.getLevel3().hasAbility(AbilitiesType.MAKER_SPY)) {
            return false;
        }

        return game.getRegions().stream()
                .flatMap(region -> region.getUnits().stream())
                .anyMatch(unit ->
                        unit.getOwnerId().equals(playerId)
                                && unit.getLevel() == 3
                                && !unit.isKilled()
                );
    }

    public void resolve(GameState game, MakerSpyDecision decision) {
        PlayerState player = game.findPlayer(decision.playerId());

        validate(game, player, decision);

        List<AbilitiesType> abilities = player.getLevel3().getAbilities();

        List<Integer> replacedIndexes = new ArrayList<>();

        int firstIndex = findFirstMakerSpyIndex(abilities);
        abilities.set(firstIndex, decision.firstAbility());
        replacedIndexes.add(firstIndex);

        int secondIndex = findFirstMakerSpyIndex(abilities);
        abilities.set(secondIndex, decision.secondAbility());
        replacedIndexes.add(secondIndex);

        game.getReplacedMakerSpyIndexesByPlayer()
                .put(decision.playerId(), replacedIndexes);
    }

    public void restore(GameState game) {
        for (var entry : game.getReplacedMakerSpyIndexesByPlayer().entrySet()) {
            UUID playerId = entry.getKey();
            List<Integer> indexes = entry.getValue();

            PlayerState player = game.findPlayer(playerId);
            List<AbilitiesType> abilities = player.getLevel3().getAbilities();

            for (Integer index : indexes) {
                if (index < 0 || index >= abilities.size()) {
                    throw new IllegalStateException("Invalid Maker Spy index");
                }

                abilities.set(index, AbilitiesType.MAKER_SPY);
            }
        }

        game.getReplacedMakerSpyIndexesByPlayer().clear();
    }

    private void validate(
            GameState game,
            PlayerState player,
            MakerSpyDecision decision
    ) {
        if (!canResolve(game, decision.playerId())) {
            throw new IllegalStateException("Maker Spy cannot be resolved");
        }

        if (decision.firstAbility() == null || decision.secondAbility() == null) {
            throw new IllegalStateException("Maker Spy abilities cannot be null");
        }

        if (decision.firstAbility() == decision.secondAbility()) {
            throw new IllegalStateException("Maker Spy abilities must be different");
        }

        if (!ALLOWED_MAKER_SPY_ABILITIES.contains(decision.firstAbility())
                || !ALLOWED_MAKER_SPY_ABILITIES.contains(decision.secondAbility())) {
            throw new IllegalStateException("Invalid Maker Spy ability");
        }

        if (game.getReplacedMakerSpyIndexesByPlayer().containsKey(decision.playerId())) {
            throw new IllegalStateException("Maker Spy already resolved this activation");
        }

        long makerSpyCount = player.getLevel3().getAbilities().stream()
                .filter(a -> a == AbilitiesType.MAKER_SPY)
                .count();

        if (makerSpyCount < 2) {
            throw new IllegalStateException("Not enough Maker Spy slots");
        }
    }

    private int findFirstMakerSpyIndex(List<AbilitiesType> abilities) {
        for (int i = 0; i < abilities.size(); i++) {
            if (abilities.get(i) == AbilitiesType.MAKER_SPY) {
                return i;
            }
        }

        throw new IllegalStateException("Maker Spy slot not found");
    }
}