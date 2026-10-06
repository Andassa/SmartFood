package com.mycompany.smartfood.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mycompany.smartfood.domain.coverage.Coverage;
import com.mycompany.smartfood.dto.RecipeInsight;
import com.mycompany.smartfood.entity.Ingredient;
import com.mycompany.smartfood.entity.MealPlan;
import com.mycompany.smartfood.entity.Recipe;

public final class WeekPlan {

    public record Placement(String date, long recipeId, int servings, String reason) {
    }

    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    private static final String[] MEALS = {"breakfast", "lunch", "dinner"};

    private WeekPlan() {
    }

    public static List<Placement> fill(List<Recipe> recipes, List<MealPlan> existing, Pantry pantry, List<Ingredient> stock) {
        if (recipes.isEmpty()) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        Set<String> expiring = new HashSet<>();
        Map<String, Integer> daysLeft = new HashMap<>();
        for (Ingredient item : stock) {
            if (item.getExpirationDate() == null) {
                continue;
            }
            int days = (int) ChronoUnit.DAYS.between(today, item.getExpirationDate());
            if (days < 0 || days > 7) {
                continue;
            }
            String key = Names.key(item.getName());
            expiring.add(key);
            daysLeft.merge(key, days, Math::min);
        }
        Map<String, MealPlan> taken = new HashMap<>();
        Map<Long, Recipe> byId = new HashMap<>();
        for (Recipe recipe : recipes) {
            byId.put(recipe.getId(), recipe);
        }
        for (MealPlan plan : existing) {
            if (plan.getDate() != null) {
                taken.putIfAbsent(plan.getDate(), plan);
            }
        }
        Map<Long, Integer> repeats = new HashMap<>();
        List<Placement> placed = new ArrayList<>();
        for (String day : DAYS) {
            for (String meal : MEALS) {
                String slot = day + "_" + meal;
                MealPlan current = taken.get(slot);
                if (current != null) {
                    spend(byId.get(current.getRecipeId()), current, pantry, repeats);
                    continue;
                }
                Recipe chosen = null;
                double best = Double.NEGATIVE_INFINITY;
                String because = "";
                for (Recipe recipe : recipes) {
                    RecipeInsight insight = Coverage.of(recipe, pantry, expiring);
                    double score = score(recipe, insight, meal, repeats, daysLeft);
                    if (score > best) {
                        best = score;
                        chosen = recipe;
                        because = reason(insight, daysLeft);
                    }
                }
                if (chosen == null) {
                    continue;
                }
                int servings = Math.max(chosen.getServings(), 1);
                MealPlan probe = new MealPlan();
                probe.setServings(servings);
                Coverage.spend(chosen, pantry, RecipeLines.scale(chosen, probe));
                repeats.merge(chosen.getId(), 1, Integer::sum);
                placed.add(new Placement(slot, chosen.getId(), servings, day + " " + meal + " · " + chosen.getName() + " · " + because));
            }
        }
        return placed;
    }

    private static void spend(Recipe recipe, MealPlan plan, Pantry pantry, Map<Long, Integer> repeats) {
        if (recipe == null) {
            return;
        }
        Coverage.spend(recipe, pantry, RecipeLines.scale(recipe, plan));
        repeats.merge(recipe.getId(), 1, Integer::sum);
    }

    private static double score(Recipe recipe, RecipeInsight insight, String meal, Map<Long, Integer> repeats, Map<String, Integer> daysLeft) {
        if (insight.getTotal() == 0) {
            return -10;
        }
        double urgency = 0;
        for (String name : insight.getExpiringUsed()) {
            urgency += Math.max(1, 8 - nearest(name, daysLeft));
        }
        int minutes = Math.max(recipe.getPrepTime(), 0) + Math.max(recipe.getCookTime(), 0);
        double slow = "breakfast".equals(meal) && minutes > 25 ? 0.7 : minutes > 75 ? 0.4 : 0;
        return insight.getScore() * 3 + urgency - repeats.getOrDefault(recipe.getId(), 0) * 0.8 - slow;
    }

    private static String reason(RecipeInsight insight, Map<String, Integer> daysLeft) {
        if (!insight.getExpiringUsed().isEmpty()) {
            String name = insight.getExpiringUsed().get(0);
            return "à finir : " + name + " (" + nearest(name, daysLeft) + " j)";
        }
        if (!insight.getSubstituted().isEmpty()) {
            return "substitution " + insight.getSubstituted().get(0);
        }
        return insight.getHave() + "/" + insight.getTotal() + " déjà au placard";
    }

    private static int nearest(String name, Map<String, Integer> daysLeft) {
        int best = 7;
        for (Map.Entry<String, Integer> entry : daysLeft.entrySet()) {
            if (Names.same(name, entry.getKey())) {
                best = Math.min(best, entry.getValue());
            }
        }
        return best;
    }
}
