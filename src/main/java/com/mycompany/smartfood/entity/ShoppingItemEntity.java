/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartfood.entity;

/**
 *
 * @author Andassa
 */
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SHOPPING_ITEM")
public class ShoppingItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private int quantity;
    
    private String unit;
    
    private boolean checked;
    private boolean fromPlan;

    public ShoppingItemEntity() { }

    public ShoppingItemEntity(String name, int quantity, String unit, boolean checked) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.checked = checked;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isFromPlan() {
        return fromPlan;
    }

    public void setFromPlan(boolean fromPlan) {
        this.fromPlan = fromPlan;
    }
}
