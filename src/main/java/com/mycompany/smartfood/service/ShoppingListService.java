package com.mycompany.smartfood.service;

import java.util.List;

import com.mycompany.smartfood.domain.Measures;
import com.mycompany.smartfood.domain.Names;
import com.mycompany.smartfood.entity.ShoppingItemEntity;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class ShoppingListService {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    @EJB
    private IngredientService ingredientService;

    public List<ShoppingItemEntity> findAll() {
        return em.createQuery("SELECT s FROM ShoppingItemEntity s ORDER BY s.id", ShoppingItemEntity.class).getResultList();
    }

    public ShoppingItemEntity create(ShoppingItemEntity item) {
        ShoppingItemEntity stored = new ShoppingItemEntity();
        apply(item, stored, false);
        stored.setFromPlan(false);
        stored.setChecked(false);
        em.persist(stored);
        return stored;
    }

    public ShoppingItemEntity update(ShoppingItemEntity item) {
        ShoppingItemEntity existing = em.find(ShoppingItemEntity.class, item.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Article introuvable");
        }
        boolean restock = item.isChecked() && !existing.isChecked();
        apply(item, existing, true);
        if (restock) {
            ingredientService.restock(existing.getName(), existing.getQuantity(), existing.getUnit());
        }
        return existing;
    }

    public void remove(Long id) {
        ShoppingItemEntity item = em.find(ShoppingItemEntity.class, id);
        if (item == null) {
            throw new IllegalArgumentException("Article introuvable");
        }
        em.remove(item);
    }

    private void apply(ShoppingItemEntity source, ShoppingItemEntity target, boolean keepChecked) {
        String name = Names.clean(source.getName(), 120);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Nom d'article requis");
        }
        if (source.getQuantity() <= 0 || source.getQuantity() > 100000) {
            throw new IllegalArgumentException("Quantité invalide");
        }
        target.setName(name);
        target.setQuantity(source.getQuantity());
        target.setUnit(Measures.canonical(source.getUnit()));
        if (keepChecked) {
            target.setChecked(source.isChecked());
        }
    }
}
