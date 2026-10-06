/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author Andassa
 */
package com.mycompany.smartfood.service;

import com.mycompany.smartfood.entity.Ingredient;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;

@Singleton
@Startup
public class NotificationService {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    @EJB
    private KitchenService kitchenService;

    @Schedule(hour = "8", minute = "0", second = "0", persistent = false)
    public void checkExpiringIngredients() {
        int removed = kitchenService.sweepExpired();
        if (removed > 0) {
            System.out.println("Ingrédients périmés archivés : " + removed);
        }
    }

    public List<Ingredient> getExpiringIngredients(int days) {
        int window = Math.min(Math.max(days, 1), 30);
        LocalDate threshold = LocalDate.now().plusDays(window);
        return em.createQuery(
                "SELECT i FROM Ingredient i WHERE i.expirationDate <= :threshold ORDER BY i.expirationDate",
                Ingredient.class)
                .setParameter("threshold", threshold)
                .getResultList();
    }
}
