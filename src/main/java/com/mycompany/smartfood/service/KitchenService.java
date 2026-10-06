package com.mycompany.smartfood.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mycompany.smartfood.catalog.NearbyShops;
import com.mycompany.smartfood.catalog.OpenFoodFacts;
import com.mycompany.smartfood.domain.Measures;
import com.mycompany.smartfood.domain.Names;
import com.mycompany.smartfood.domain.Pantry;
import com.mycompany.smartfood.domain.ShoppingPlan;
import com.mycompany.smartfood.domain.WeekBalance;
import com.mycompany.smartfood.domain.WeekPlan;
import com.mycompany.smartfood.domain.coverage.Coverage;
import com.mycompany.smartfood.dto.ProductLookup;
import com.mycompany.smartfood.dto.ShopRoute;
import com.mycompany.smartfood.dto.RecipeInsight;
import com.mycompany.smartfood.dto.WeekProposal;
import com.mycompany.smartfood.dto.WeekReport;
import com.mycompany.smartfood.entity.Ingredient;
import com.mycompany.smartfood.entity.MealPlan;
import com.mycompany.smartfood.entity.Recipe;
import com.mycompany.smartfood.entity.ShoppingItemEntity;
import com.mycompany.smartfood.entity.WasteEntry;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class KitchenService {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public List<RecipeInsight> coverage() {
        Pantry pantry = pantry();
        return recipes().stream()
                .map(recipe -> Coverage.of(recipe, pantry, Set.of()))
                .sorted(Comparator.comparingDouble(RecipeInsight::getScore).reversed())
                .toList();
    }

    public List<RecipeInsight> suggestions(int days) {
        int window = Math.min(Math.max(days, 1), 30);
        LocalDate today = LocalDate.now();
        Set<String> expiring = ingredients().stream()
                .filter(item -> within(item, today, today.plusDays(window)))
                .map(item -> Names.key(item.getName()))
                .collect(Collectors.toSet());
        Pantry pantry = pantry();
        return recipes().stream()
                .map(recipe -> Coverage.of(recipe, pantry, expiring))
                .filter(insight -> !insight.getExpiringUsed().isEmpty())
                .sorted(Comparator.comparingInt((RecipeInsight insight) -> insight.getExpiringUsed().size()).reversed()
                        .thenComparing(Comparator.comparingDouble(RecipeInsight::getScore).reversed()))
                .toList();
    }

    public List<ShoppingItemEntity> planShopping() {
        Map<Long, Recipe> recipes = recipes().stream().collect(Collectors.toMap(Recipe::getId, recipe -> recipe));
        List<MealPlan> plans = em.createQuery("SELECT m FROM MealPlan m", MealPlan.class).getResultList();
        for (ShoppingItemEntity item : generatedItems()) {
            em.remove(item);
        }
        em.flush();
        for (ShoppingPlan.Item missing : ShoppingPlan.of(plans, recipes, pantry())) {
            ShoppingItemEntity item = new ShoppingItemEntity(missing.name(), missing.quantity(), missing.unit(), false);
            item.setFromPlan(true);
            em.persist(item);
        }
        return em.createQuery("SELECT s FROM ShoppingItemEntity s ORDER BY s.id", ShoppingItemEntity.class).getResultList();
    }

    public List<WasteEntry> waste() {
        return em.createQuery("SELECT w FROM WasteEntry w ORDER BY w.wastedOn DESC, w.id DESC", WasteEntry.class)
                .getResultList();
    }

    public int sweepExpired() {
        LocalDate today = LocalDate.now();
        List<Ingredient> expired = em.createQuery(
                "SELECT i FROM Ingredient i WHERE i.expirationDate < :today", Ingredient.class)
                .setParameter("today", today)
                .getResultList();
        for (Ingredient ingredient : expired) {
            WasteEntry entry = new WasteEntry();
            entry.setName(ingredient.getName());
            entry.setQuantity(Math.max(ingredient.getQuantity(), 0));
            entry.setUnit(Measures.canonical(ingredient.getUnit()));
            entry.setWastedOn(today);
            entry.setReason("EXPIRED");
            em.persist(entry);
            em.remove(ingredient);
        }
        return expired.size();
    }

    public ProductLookup lookupBarcode(String code) {
        return OpenFoodFacts.lookup(code);
    }

    public ShopRoute markets(double latitude, double longitude) {
        if (Double.isNaN(latitude) || latitude < -90 || latitude > 90 || Double.isNaN(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Position invalide");
        }
        return NearbyShops.plan(latitude, longitude, basket(), expiringSoon());
    }

    public ShopRoute guide() {
        return NearbyShops.guide(basket());
    }

    public WeekProposal proposeWeek() {
        List<MealPlan> plans = em.createQuery("SELECT m FROM MealPlan m", MealPlan.class).getResultList();
        WeekProposal proposal = new WeekProposal();
        for (WeekPlan.Placement placement : WeekPlan.fill(recipes(), plans, pantry(), ingredients())) {
            MealPlan plan = new MealPlan();
            plan.setDate(placement.date());
            plan.setRecipeId(placement.recipeId());
            plan.setServings(placement.servings());
            em.persist(plan);
            proposal.getReasons().add(placement.reason());
        }
        proposal.setAdded(proposal.getReasons().size());
        return proposal;
    }

    public WeekReport report() {
        Map<Long, Recipe> recipes = recipes().stream().collect(Collectors.toMap(Recipe::getId, recipe -> recipe));
        List<MealPlan> plans = em.createQuery("SELECT m FROM MealPlan m", MealPlan.class).getResultList();
        return WeekBalance.of(plans, recipes, ingredients(), waste());
    }

    private Pantry pantry() {
        LocalDate today = LocalDate.now();
        List<Ingredient> usable = ingredients().stream()
                .filter(item -> item.getExpirationDate() == null || !item.getExpirationDate().isBefore(today))
                .toList();
        return Pantry.of(usable);
    }

    private boolean within(Ingredient item, LocalDate from, LocalDate to) {
        LocalDate date = item.getExpirationDate();
        return date != null && !date.isBefore(from) && !date.isAfter(to);
    }

    private List<ShoppingItemEntity> generatedItems() {
        return em.createQuery(
                "SELECT s FROM ShoppingItemEntity s WHERE s.fromPlan = true AND s.checked = false",
                ShoppingItemEntity.class).getResultList();
    }

    private List<Recipe> recipes() {
        return em.createQuery("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.lines", Recipe.class).getResultList();
    }

    private List<Ingredient> ingredients() {
        return em.createQuery("SELECT i FROM Ingredient i", Ingredient.class).getResultList();
    }

    private List<String> basket() {
        return em.createQuery(
                "SELECT s.name FROM ShoppingItemEntity s WHERE s.checked = false ORDER BY s.id", String.class)
                .getResultList();
    }

    private String expiringSoon() {
        LocalDate today = LocalDate.now();
        return ingredients().stream()
                .filter(item -> within(item, today, today.plusDays(7)))
                .min(Comparator.comparing(Ingredient::getExpirationDate))
                .map(Ingredient::getName)
                .orElse(null);
    }
}
