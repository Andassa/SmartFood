package com.mycompany.smartfood.dto;

public class ProductLookup {

    private String barcode;
    private String name;
    private int quantity;
    private String unit;

    public ProductLookup() {
    }

    public ProductLookup(String barcode, String name, int quantity, String unit) {
        this.barcode = barcode;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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
}
