/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartfood.service;

/**
 *
 * @author Mahery
 */
import com.mycompany.smartfood.entity.Ingredient;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class IngredientService {
    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public List<Ingredient> findAll() {
        return em.createQuery("SELECT i FROM Ingredient i", Ingredient.class).getResultList();
    }

    public Ingredient create(Ingredient ingredient) {
        // Validation simple : nom non vide et quantité positive
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty() || ingredient.getQuantity() < 0) {
            throw new IllegalArgumentException("Données d'ingrédient invalides");
        }
        em.persist(ingredient);
        return ingredient;
    }

    public Ingredient update(Ingredient ingredient) {
        Ingredient existing = em.find(Ingredient.class, ingredient.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Ingrédient non trouvé");
        }
        // Validation : pas de quantité négative
        if (ingredient.getQuantity() < 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        return em.merge(ingredient);
    }

    public void remove(Long id) {
        Ingredient ingredient = em.find(Ingredient.class, id);
        if (ingredient != null) {
            em.remove(ingredient);
        } else {
            throw new IllegalArgumentException("Ingrédient non trouvé");
        }
    }
}