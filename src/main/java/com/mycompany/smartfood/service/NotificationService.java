/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author Mahery
 */
package com.mycompany.smartfood.service;

import com.mycompany.smartfood.entity.Ingredient;
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

    // Exécution quotidienne par défaut à 8h00 avec un seuil de 2 jours
    @Schedule(hour = "8", minute = "0", second = "0", persistent = false)
    public void checkExpiringIngredients() {
         int defaultDays = 2;
         LocalDate threshold = LocalDate.now().plusDays(defaultDays);
         List<Ingredient> expiring = em.createQuery(
             "SELECT i FROM Ingredient i WHERE i.expirationDate <= :threshold", Ingredient.class)
             .setParameter("threshold", threshold)
             .getResultList();
         if (!expiring.isEmpty()){
             System.out.println("Ingrédients proches de la péremption : " + expiring);
         }
    }
    
    public List<Ingredient> getExpiringIngredients(int days) {
         LocalDate threshold = LocalDate.now().plusDays(days);
         return em.createQuery(
             "SELECT i FROM Ingredient i WHERE i.expirationDate <= :threshold", Ingredient.class)
             .setParameter("threshold", threshold)
             .getResultList();
    }
}
