package com.game.game.domain.action;

import com.game.game.domain.PlayerState;
import com.game.game.domain.ViperGorge;

import java.util.List;
import java.util.UUID;

public class ViperGorgeResolver {

    public void resolvePlayer(
            PlayerState player,
            ViperGorge viperGorge,
            ViperGorgeDecision decision
    ) {

        UUID playerId = player.getPlayerId();

        int markersCount = (int) viperGorge.getActionMarkers().stream()
                .filter(marker -> marker.getPlayerId().equals(playerId))
                .count();

        // =====================================================
        // VALIDATE TAKE RESOURCES
        // =====================================================

        List<ResourceType> takenResources = decision.takenResources();

        if (takenResources.size() > markersCount) {
            throw new IllegalStateException(
                    "Cannot take more resources than action markers in Viper Gorge"
            );
        }

        // =====================================================
        // VALIDATE EXCHANGES (GLOBALNIE)
        // =====================================================

        int gold = player.getGold();
        int population = player.getPopulation();
        int mana0 = player.getMana(0);

        for (ExchangeDecision exchange : decision.exchanges()) {

            removeTempResource(exchange.first(), player, Holder.of(gold), Holder.of(population), Holder.of(mana0));
            removeTempResource(exchange.second(), player, Holder.of(gold), Holder.of(population), Holder.of(mana0));
        }

        // =====================================================
        // APPLY TAKE RESOURCES
        // =====================================================

        for (ResourceType resource : takenResources) {
            addResource(player, resource, 1);
        }

        // =====================================================
        // APPLY EXCHANGES
        // =====================================================

        for (ExchangeDecision exchange : decision.exchanges()) {

            spendResource(player, exchange.first());
            spendResource(player, exchange.second());

            addResource(player, exchange.result(), 1);
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void spendResource(PlayerState player, ResourceType type) {
        switch (type) {
            case GOLD -> player.spendGold(1);
            case POPULATION -> player.spendPopulation(1);
            case MANA -> player.spendMana(0, 1);
        }
    }

    private void addResource(PlayerState player, ResourceType type, int amount) {
        switch (type) {
            case GOLD -> player.addGold(amount);
            case POPULATION -> player.addPopulation(amount);
            case MANA -> player.addMana(0, amount);
        }
    }

    /*
     * WALIDACJA GLOBALNA:
     * sprawdzamy wszystkie wymiany zanim zaczniemy je wykonywać
     */
    private void removeTempResource(
            ResourceType type,
            PlayerState player,
            Holder<Integer> gold,
            Holder<Integer> population,
            Holder<Integer> mana0
    ) {
        switch (type) {
            case GOLD -> {
                if (gold.value <= 0) {
                    throw new IllegalStateException("Not enough gold for exchange");
                }
                gold.value--;
            }

            case POPULATION -> {
                if (population.value <= 0) {
                    throw new IllegalStateException("Not enough population for exchange");
                }
                population.value--;
            }

            case MANA -> {
                if (mana0.value <= 0) {
                    throw new IllegalStateException("Not enough mana level 0 for exchange");
                }
                mana0.value--;
            }
        }
    }

    private static class Holder<T> {
        T value;

        private Holder(T value) {
            this.value = value;
        }

        static <T> Holder<T> of(T value) {
            return new Holder<>(value);
        }
    }
}