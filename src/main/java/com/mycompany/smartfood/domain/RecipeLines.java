package com.mycompany.smartfood.domain;

import java.util.ArrayList;
import java.util.List;

import com.mycompany.smartfood.entity.MealPlan;
import com.mycompany.smartfood.entity.Recipe;
import com.mycompany.smartfood.entity.RecipeComponent;

public final class RecipeLines {

    private RecipeLines() {
    }

    public static List<RecipeComponent> of(Recipe recipe) {
        if (recipe.getLines() != null && !recipe.getLines().isEmpty()) {
            return recipe.getLines();
        }
        List<RecipeComponent> lines = new ArrayList<>();
        if (recipe.getIngredients() == null) {
            return lines;
        }
        for (String name : recipe.getIngredients()) {
            if (name == null || name.isBlank()) {
                continue;
            }
            RecipeComponent line = new RecipeComponent();
            line.setName(name);
            line.setQuantity(1);
            line.setUnit("pcs");
            lines.add(line);
        }
        return lines;
    }

    public static double scale(Recipe recipe, MealPlan plan) {
        int recipeServings = Math.max(recipe.getServings(), 1);
        int mealServings = plan.getServings() > 0 ? plan.getServings() : recipeServings;
        return mealServings / (double) recipeServings;
    }
}
