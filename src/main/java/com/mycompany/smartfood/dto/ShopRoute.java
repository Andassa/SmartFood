package com.mycompany.smartfood.dto;

import java.util.ArrayList;
import java.util.List;

public class ShopRoute {

    private String advice;
    private List<ShopStop> stops = new ArrayList<>();

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public List<ShopStop> getStops() {
        return stops;
    }

    public void setStops(List<ShopStop> stops) {
        this.stops = stops;
    }
}
