/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author Mahery
 */

package com.mycompany.smartfood.service;
import com.mycompany.smartfood.entity.Recipe;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class RecipeService {
    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public List<Recipe> findAll() {
        return em.createQuery("SELECT r FROM Recipe r", Recipe.class).getResultList();
    }
    
    public Recipe create(Recipe recipe) {
        em.persist(recipe);
        return recipe;
    }
    
    public Recipe update(Recipe recipe) {
        Recipe existing = em.find(Recipe.class, recipe.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Recipe not found");
        }
        return em.merge(recipe);
    }
    
    public void remove(Long id) {
        Recipe recipe = em.find(Recipe.class, id);
        if (recipe != null) {
            em.remove(recipe);
        } else {
            throw new IllegalArgumentException("Recipe not found");
        }
    }
    
    public List<Recipe> recommendRecipes(List<String> availableIngredients) {
        List<Recipe> allRecipes = findAll();
        return allRecipes.stream()
          .filter(recipe -> recipe.getIngredients() != null &&
                availableIngredients.containsAll(recipe.getIngredients()))
          .collect(Collectors.toList());
    }
}