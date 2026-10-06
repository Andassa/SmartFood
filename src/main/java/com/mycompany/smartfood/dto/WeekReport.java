package com.mycompany.smartfood.dto;

import java.util.ArrayList;
import java.util.List;

public class WeekReport {

    private int kcal;
    private int protein;
    private int meals;
    private List<String> atRisk = new ArrayList<>();
    private List<String> habits = new ArrayList<>();

    public int getKcal() {
        return kcal;
    }

    public void setKcal(int kcal) {
        this.kcal = kcal;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getMeals() {
        return meals;
    }

    public void setMeals(int meals) {
        this.meals = meals;
    }

    public List<String> getAtRisk() {
        return atRisk;
    }

    public void setAtRisk(List<String> atRisk) {
        this.atRisk = atRisk;
    }

    public List<String> getHabits() {
        return habits;
    }

    public void setHabits(List<String> habits) {
        this.habits = habits;
    }
}
