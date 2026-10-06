package com.mycompany.smartfood.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mycompany.smartfood.catalog.OpenFoodFacts;
import com.mycompany.smartfood.catalog.OpenFoodFacts.Per100g;
import com.mycompany.smartfood.domain.Measures.Amount;
import com.mycompany.smartfood.domain.Measures.Dimension;
import com.mycompany.smartfood.dto.WeekReport;
import com.mycompany.smartfood.entity.Ingredient;
import com.mycompany.smartfood.entity.MealPlan;
import com.mycompany.smartfood.entity.Recipe;
import com.mycompany.smartfood.entity.RecipeComponent;
import com.mycompany.smartfood.entity.WasteEntry;

public final class WeekBalance {

    private WeekBalance() {
    }

    public static WeekReport of(List<MealPlan> plans, Map<Long, Recipe> recipes, List<Ingredient> stock, List<WasteEntry> waste) {
        WeekReport report = new WeekReport();
        report.setMeals(plans.size());
        double kcal = 0;
        double protein = 0;
        long deadline = System.nanoTime() + 6_000_000_000L;
        Map<String, Optional<Per100g>> nutrients = new HashMap<>();
        Map<String, Boolean> used = new HashMap<>();
        for (MealPlan plan : plans) {
            Recipe recipe = recipes.get(plan.getRecipeId());
            if (recipe == null) {
                continue;
            }
            double scale = RecipeLines.scale(recipe, plan);
            for (RecipeComponent line : RecipeLines.of(recipe)) {
                used.put(Names.key(line.getName()), true);
                Ingredient source = withBarcode(line.getName(), stock);
                if (source == null) {
                    continue;
                }
                Per100g per = nutrients.computeIfAbsent(source.getBarcode(), code -> load(code, deadline)).orElse(null);
                if (per == null) {
                    continue;
                }
                Amount amount = Measures.toBase(line.getQuantity() * scale, line.getUnit());
                if (amount.dimension() == Dimension.COUNT) {
                    continue;
                }
                kcal += amount.base() * per.kcal() / 100;
                protein += amount.base() * per.protein() / 100;
            }
        }
        report.setKcal((int) Math.round(kcal));
        report.setProtein((int) Math.round(protein));
        LocalDate today = LocalDate.now();
        for (Ingredient item : stock) {
            if (item.getExpirationDate() == null || Names.hits(item.getName(), used.keySet())) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(today, item.getExpirationDate());
            if (days >= 0 && days <= 7) {
                report.getAtRisk().add(item.getName() + " (" + days + " j)");
            }
            int thrown = 0;
            for (WasteEntry entry : waste) {
                if (Names.same(item.getName(), entry.getName())) {
                    thrown++;
                }
            }
            if (thrown >= 2) {
                report.getHabits().add(item.getName() + " jeté " + thrown + " fois");
            }
        }
        return report;
    }

    private static Optional<Per100g> load(String code, long deadline) {
        if (System.nanoTime() > deadline) {
            return Optional.empty();
        }
        return Optional.ofNullable(OpenFoodFacts.per100g(code));
    }

    private static Ingredient withBarcode(String name, List<Ingredient> stock) {
        Ingredient found = null;
        for (Ingredient item : stock) {
            if (item.getBarcode() == null || item.getBarcode().isBlank() || !Names.same(name, item.getName())) {
                continue;
            }
            found = item;
        }
        return found;
    }
}
