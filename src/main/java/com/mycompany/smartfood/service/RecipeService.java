package com.mycompany.smartfood.service;

import java.util.ArrayList;
import java.util.List;

import com.mycompany.smartfood.domain.Measures;
import com.mycompany.smartfood.domain.Names;
import com.mycompany.smartfood.entity.Recipe;
import com.mycompany.smartfood.entity.RecipeComponent;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class RecipeService {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public List<Recipe> findAll() {
        return em.createQuery("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.lines", Recipe.class).getResultList();
    }

    public Recipe create(Recipe recipe) {
        Recipe stored = new Recipe();
        copy(recipe, stored);
        em.persist(stored);
        return stored;
    }

    public Recipe update(Recipe recipe) {
        Recipe existing = em.find(Recipe.class, recipe.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Recette introuvable");
        }
        copy(recipe, existing);
        return existing;
    }

    public void remove(Long id) {
        Recipe recipe = em.find(Recipe.class, id);
        if (recipe == null) {
            throw new IllegalArgumentException("Recette introuvable");
        }
        em.remove(recipe);
    }

    public List<Recipe> recommendRecipes(List<String> availableIngredients) {
        List<String> keys = availableIngredients == null
                ? List.of()
                : availableIngredients.stream().map(Names::key).toList();
        return findAll().stream()
                .filter(recipe -> recipe.getLines() != null && recipe.getLines().stream()
                        .allMatch(line -> keys.contains(Names.key(line.getName()))))
                .toList();
    }

    private void copy(Recipe source, Recipe target) {
        String name = Names.clean(source.getName(), 120);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Nom de recette requis");
        }
        if (source.getPrepTime() < 0 || source.getCookTime() < 0 || source.getPrepTime() > 24 * 60 || source.getCookTime() > 24 * 60) {
            throw new IllegalArgumentException("Durée invalide");
        }
        int servings = source.getServings() <= 0 ? 1 : source.getServings();
        if (servings > 50) {
            throw new IllegalArgumentException("Nombre de portions invalide");
        }
        target.setName(name);
        target.setDescription(Names.clean(source.getDescription(), 500));
        target.setPrepTime(source.getPrepTime());
        target.setCookTime(source.getCookTime());
        target.setServings(servings);
        if (target.getLines() == null) {
            target.setLines(new ArrayList<>());
        }
        boolean managed = target.getId() != null;
        target.getLines().clear();
        if (managed) {
            em.flush();
        }
        List<RecipeComponent> incoming = source.getLines();
        if (incoming == null || incoming.isEmpty()) {
            if (source.getIngredients() != null) {
                for (String ingredient : source.getIngredients()) {
                    addLine(target, ingredient, 1, "pcs");
                }
            }
        } else {
            if (incoming.size() > 40) {
                throw new IllegalArgumentException("Trop d'ingrédients");
            }
            for (RecipeComponent line : incoming) {
                addLine(target, line.getName(), line.getQuantity(), line.getUnit());
            }
        }
        if (target.getLines().isEmpty()) {
            throw new IllegalArgumentException("Au moins un ingrédient est requis");
        }
        List<String> steps = source.getSteps() == null ? List.of() : source.getSteps().stream()
                .map(step -> Names.clean(step, 500))
                .filter(step -> !step.isEmpty())
                .toList();
        if (steps.isEmpty() || steps.size() > 40) {
            throw new IllegalArgumentException("Étapes invalides");
        }
        target.setSteps(new ArrayList<>(steps));
        target.setIngredients(target.getLines().stream().map(RecipeComponent::getName).toList());
    }

    private void addLine(Recipe recipe, String name, double quantity, String unit) {
        String cleaned = Names.clean(name, 120);
        if (cleaned.isEmpty()) {
            return;
        }
        if (quantity <= 0 || quantity > 100000) {
            throw new IllegalArgumentException("Quantité de recette invalide");
        }
        RecipeComponent line = new RecipeComponent();
        line.setRecipe(recipe);
        line.setName(cleaned);
        line.setQuantity(quantity);
        line.setUnit(Measures.canonical(unit));
        recipe.getLines().add(line);
    }
}
