/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartfood.service;

import com.mycompany.smartfood.entity.ShoppingItemEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class ShoppingListService {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;
    
    public List<ShoppingItemEntity> findAll() {
        return em.createQuery("SELECT s FROM ShoppingItemEntity s", ShoppingItemEntity.class)
                 .getResultList();
    }

    public ShoppingItemEntity create(ShoppingItemEntity item) {
        em.persist(item);
        return item;
    }
    
    public ShoppingItemEntity update(ShoppingItemEntity item) {
        ShoppingItemEntity existing = em.find(ShoppingItemEntity.class, item.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Item not found");
        }
        return em.merge(item);
    }
    
    public void remove(Long id) {
        ShoppingItemEntity item = em.find(ShoppingItemEntity.class, id);
        if (item != null) {
            em.remove(item);
        }
    }
}
