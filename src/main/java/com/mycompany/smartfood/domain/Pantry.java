package com.mycompany.smartfood.domain;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mycompany.smartfood.domain.Measures.Amount;
import com.mycompany.smartfood.domain.Measures.Dimension;
import com.mycompany.smartfood.entity.Ingredient;

public final class Pantry {

    private final Map<String, EnumMap<Dimension, Double>> stock = new HashMap<>();

    public static Pantry of(List<Ingredient> items) {
        Pantry pantry = new Pantry();
        for (Ingredient item : items) {
            if (item.getQuantity() <= 0) {
                continue;
            }
            pantry.add(Names.key(item.getName()), Measures.toBase(item.getQuantity(), item.getUnit()));
        }
        return pantry;
    }

    public Pantry copy() {
        Pantry copy = new Pantry();
        stock.forEach((key, amounts) -> copy.stock.put(key, new EnumMap<>(amounts)));
        return copy;
    }

    public boolean take(String key, Amount need) {
        EnumMap<Dimension, Double> amounts = stock.get(key);
        double available = amounts == null ? 0 : amounts.getOrDefault(need.dimension(), 0d);
        if (available + 0.001 < need.base()) {
            return false;
        }
        amounts.merge(need.dimension(), -need.base(), Double::sum);
        return true;
    }

    public String resolve(String name, Dimension dimension) {
        String exact = Names.key(name);
        if (available(exact, dimension) > 0) {
            return exact;
        }
        String best = null;
        double bestQty = 0;
        for (String key : stock.keySet()) {
            if (key.equals(exact) || !Names.same(name, key)) {
                continue;
            }
            double qty = available(key, dimension);
            if (qty > bestQty) {
                best = key;
                bestQty = qty;
            }
        }
        if (best != null) {
            return best;
        }
        return stock.containsKey(exact) ? exact : null;
    }

    public double available(String key, Dimension dimension) {
        EnumMap<Dimension, Double> amounts = stock.get(key);
        return amounts == null ? 0 : amounts.getOrDefault(dimension, 0d);
    }

    private void add(String key, Amount amount) {
        stock.computeIfAbsent(key, ignored -> new EnumMap<>(Dimension.class))
                .merge(amount.dimension(), amount.base(), Double::sum);
    }
}
