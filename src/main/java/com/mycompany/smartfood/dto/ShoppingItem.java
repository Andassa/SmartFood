/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartfood.dto;

/**
 *
 * @author Mahery
 */
public class ShoppingItem {
    private String name;
    private int quantity;
    private String unit;
    private boolean checked;
    
    public ShoppingItem(String name, int quantity, String unit, boolean checked) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.checked = checked;
    }

    // Getters et setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}