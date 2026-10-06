package com.mycompany.smartfood.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mycompany.smartfood.domain.Measures.Amount;
import com.mycompany.smartfood.domain.Measures.Dimension;
import com.mycompany.smartfood.entity.MealPlan;
import com.mycompany.smartfood.entity.Recipe;
import com.mycompany.smartfood.entity.RecipeComponent;

public final class ShoppingPlan {

    public record Item(String name, int quantity, String unit) {
    }

    private ShoppingPlan() {
    }

    public static List<Item> of(List<MealPlan> plans, Map<Long, Recipe> recipes, Pantry pantry) {
        Map<String, Need> needs = new LinkedHashMap<>();
        for (MealPlan plan : plans) {
            Recipe recipe = recipes.get(plan.getRecipeId());
            if (recipe == null) {
                continue;
            }
            double scale = RecipeLines.scale(recipe, plan);
            for (RecipeComponent line : RecipeLines.of(recipe)) {
                Amount amount = Measures.toBase(line.getQuantity() * scale, line.getUnit());
                String key = Names.key(line.getName()) + "|" + amount.dimension();
                needs.computeIfAbsent(key, ignored -> new Need(line.getName(), Measures.canonical(line.getUnit()), amount.dimension()))
                        .add(amount.base());
            }
        }
        List<Item> missing = new ArrayList<>();
        for (Need need : needs.values()) {
            String stockKey = pantry.resolve(need.name, need.dimension);
            double have = stockKey == null ? 0 : pantry.available(stockKey, need.dimension);
            double gap = Math.max(0, need.base - have);
            if (stockKey != null && have > 0) {
                pantry.take(stockKey, new Amount(need.dimension, Math.min(have, need.base)));
            }
            int quantity = (int) Math.ceil(Measures.fromBase(gap, need.unit));
            if (quantity > 0 && gap > 0.01) {
                missing.add(new Item(need.name, quantity, need.unit));
            }
        }
        return missing;
    }

    private static final class Need {
        private final String name;
        private final String unit;
        private final Dimension dimension;
        private double base;

        private Need(String name, String unit, Dimension dimension) {
            this.name = name;
            this.unit = unit;
            this.dimension = dimension;
        }

        private void add(double value) {
            base += value;
        }
    }
}
