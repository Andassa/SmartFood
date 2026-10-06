package com.mycompany.smartfood.service;

import java.time.LocalDate;
import java.util.List;

import com.mycompany.smartfood.domain.Measures;
import com.mycompany.smartfood.domain.Names;
import com.mycompany.smartfood.entity.Ingredient;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class IngredientService {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public List<Ingredient> findAll() {
        return em.createQuery("SELECT i FROM Ingredient i ORDER BY i.expirationDate", Ingredient.class).getResultList();
    }

    public Ingredient create(Ingredient ingredient) {
        Ingredient stored = new Ingredient();
        apply(ingredient, stored);
        em.persist(stored);
        return stored;
    }

    public Ingredient update(Ingredient ingredient) {
        Ingredient existing = em.find(Ingredient.class, ingredient.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Ingrédient introuvable");
        }
        apply(ingredient, existing);
        return existing;
    }

    public void remove(Long id) {
        Ingredient ingredient = em.find(Ingredient.class, id);
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingrédient introuvable");
        }
        em.remove(ingredient);
    }

    public void restock(String name, int quantity, String unit) {
        if (quantity <= 0) {
            return;
        }
        String key = Names.key(name);
        String canonical = Measures.canonical(unit);
        for (Ingredient ingredient : findAll()) {
            if (key.equals(Names.key(ingredient.getName())) && canonical.equals(Measures.canonical(ingredient.getUnit()))) {
                ingredient.setQuantity(ingredient.getQuantity() + quantity);
                if (ingredient.getExpirationDate() == null || ingredient.getExpirationDate().isBefore(LocalDate.now())) {
                    ingredient.setExpirationDate(LocalDate.now().plusDays(14));
                }
                return;
            }
        }
        Ingredient created = new Ingredient();
        created.setName(Names.clean(name, 120));
        created.setQuantity(quantity);
        created.setUnit(canonical);
        created.setExpirationDate(LocalDate.now().plusDays(14));
        em.persist(created);
    }

    private void apply(Ingredient source, Ingredient target) {
        String name = Names.clean(source.getName(), 120);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Nom d'ingrédient requis");
        }
        if (source.getQuantity() <= 0 || source.getQuantity() > 100000) {
            throw new IllegalArgumentException("Quantité invalide");
        }
        if (source.getExpirationDate() == null) {
            throw new IllegalArgumentException("Date de péremption requise");
        }
        String barcode = source.getBarcode() == null ? "" : source.getBarcode().trim();
        if (!barcode.isEmpty() && !barcode.matches("\\d{8,14}")) {
            throw new IllegalArgumentException("Code-barres invalide");
        }
        target.setName(name);
        target.setQuantity(source.getQuantity());
        target.setUnit(Measures.canonical(source.getUnit()));
        target.setExpirationDate(source.getExpirationDate());
        target.setBarcode(barcode.isEmpty() ? null : barcode);
    }
}
