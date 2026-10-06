package com.mycompany.smartfood.domain.coverage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mycompany.smartfood.domain.Measures;
import com.mycompany.smartfood.domain.Names;
import com.mycompany.smartfood.domain.Pantry;
import com.mycompany.smartfood.domain.RecipeLines;
import com.mycompany.smartfood.domain.coverage.LineHandler.Match;
import com.mycompany.smartfood.dto.RecipeInsight;
import com.mycompany.smartfood.entity.Recipe;
import com.mycompany.smartfood.entity.RecipeComponent;

public final class Coverage {

    private static final LineHandler CHAIN = new StockMatch(new SubstitutionMatch(null));

    private Coverage() {
    }

    public static RecipeInsight of(Recipe recipe, Pantry pantry, Set<String> expiring) {
        List<RecipeComponent> lines = RecipeLines.of(recipe);
        RecipeInsight insight = new RecipeInsight();
        insight.setRecipeId(recipe.getId());
        insight.setRecipeName(recipe.getName());
        insight.setTotal(lines.size());
        Pantry local = pantry.copy();
        int have = 0;
        List<String> missing = new ArrayList<>();
        List<String> swapped = new ArrayList<>();
        List<String> expiringUsed = new ArrayList<>();
        for (RecipeComponent line : lines) {
            Match match = CHAIN.handle(new LineHandler.Request(
                    line.getName(),
                    Measures.toBase(Math.max(line.getQuantity(), 0), line.getUnit()),
                    local));
            if (match.kind() == Match.Kind.MISSING) {
                missing.add(line.getName());
                continue;
            }
            have++;
            if (match.kind() == Match.Kind.SWAPPED) {
                swapped.add(match.note());
                String target = match.note().substring(match.note().lastIndexOf('→') + 1).trim();
                if (Names.hits(target, expiring)) {
                    expiringUsed.add(target);
                }
            } else if (Names.hits(line.getName(), expiring)) {
                expiringUsed.add(line.getName());
            }
        }
        insight.setHave(have);
        insight.setScore(lines.isEmpty() ? 0 : (double) have / lines.size());
        insight.setMissing(missing);
        insight.setSubstituted(swapped);
        insight.setExpiringUsed(expiringUsed);
        return insight;
    }

    public static void spend(Recipe recipe, Pantry pantry, double scale) {
        for (RecipeComponent line : RecipeLines.of(recipe)) {
            CHAIN.handle(new LineHandler.Request(
                    line.getName(),
                    Measures.toBase(Math.max(line.getQuantity(), 0) * scale, line.getUnit()),
                    pantry));
        }
    }
}
