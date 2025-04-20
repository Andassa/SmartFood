/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartfood.service;

/**
 *
 * @author Mahery
 */
import com.mycompany.smartfood.entity.MealPlan;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class MealPlanService {
    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public List<MealPlan> findAll() {
        return em.createQuery("SELECT m FROM MealPlan m", MealPlan.class).getResultList();
    }

    public MealPlan create(MealPlan mealPlan) {
        em.persist(mealPlan);
        return mealPlan;
    }
    
    public MealPlan update(MealPlan mealPlan) {
        MealPlan existing = em.find(MealPlan.class, mealPlan.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Plan de repas non trouvé");
        }
        return em.merge(mealPlan);
    }
    
    public void remove(Long id) {
        MealPlan mealPlan = em.find(MealPlan.class, id);
        if (mealPlan != null) {
            em.remove(mealPlan);
        }
    }
}