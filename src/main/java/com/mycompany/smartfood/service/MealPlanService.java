package com.mycompany.smartfood.service;

import java.util.List;
import java.util.Set;

import com.mycompany.smartfood.entity.MealPlan;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class MealPlanService {

    private static final Set<String> DAYS = Set.of(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static final Set<String> MEALS = Set.of("breakfast", "lunch", "dinner");

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public List<MealPlan> findAll() {
        return em.createQuery("SELECT m FROM MealPlan m ORDER BY m.id", MealPlan.class).getResultList();
    }

    public MealPlan create(MealPlan mealPlan) {
        MealPlan stored = new MealPlan();
        apply(mealPlan, stored);
        em.persist(stored);
        return stored;
    }

    public MealPlan update(MealPlan mealPlan) {
        MealPlan existing = em.find(MealPlan.class, mealPlan.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Menu introuvable");
        }
        apply(mealPlan, existing);
        return existing;
    }

    public void remove(Long id) {
        MealPlan mealPlan = em.find(MealPlan.class, id);
        if (mealPlan == null) {
            throw new IllegalArgumentException("Menu introuvable");
        }
        em.remove(mealPlan);
    }

    private void apply(MealPlan source, MealPlan target) {
        if (source.getDate() == null || !source.getDate().matches("^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)_(breakfast|lunch|dinner)$")) {
            throw new IllegalArgumentException("Créneau de repas invalide");
        }
        if (source.getRecipeId() == null || em.find(com.mycompany.smartfood.entity.Recipe.class, source.getRecipeId()) == null) {
            throw new IllegalArgumentException("Recette introuvable");
        }
        if (source.getServings() < 0 || source.getServings() > 50) {
            throw new IllegalArgumentException("Nombre de portions invalide");
        }
        String day = source.getDate().substring(0, source.getDate().indexOf('_'));
        String meal = source.getDate().substring(source.getDate().indexOf('_') + 1);
        if (!DAYS.contains(day) || !MEALS.contains(meal)) {
            throw new IllegalArgumentException("Créneau de repas invalide");
        }
        target.setDate(source.getDate());
        target.setRecipeId(source.getRecipeId());
        target.setServings(source.getServings());
    }
}
