package com.mycompany.smartfood.dto;

import java.util.ArrayList;
import java.util.List;

public class RecipeInsight {

    private Long recipeId;
    private String recipeName;
    private int have;
    private int total;
    private double score;
    private List<String> expiringUsed = new ArrayList<>();
    private List<String> missing = new ArrayList<>();
    private List<String> substituted = new ArrayList<>();

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public int getHave() {
        return have;
    }

    public void setHave(int have) {
        this.have = have;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<String> getExpiringUsed() {
        return expiringUsed;
    }

    public void setExpiringUsed(List<String> expiringUsed) {
        this.expiringUsed = expiringUsed;
    }

    public List<String> getMissing() {
        return missing;
    }

    public void setMissing(List<String> missing) {
        this.missing = missing;
    }

    public List<String> getSubstituted() {
        return substituted;
    }

    public void setSubstituted(List<String> substituted) {
        this.substituted = substituted;
    }
}
