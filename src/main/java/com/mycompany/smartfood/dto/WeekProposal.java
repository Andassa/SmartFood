package com.mycompany.smartfood.dto;

import java.util.ArrayList;
import java.util.List;

public class WeekProposal {

    private int added;
    private List<String> reasons = new ArrayList<>();

    public int getAdded() {
        return added;
    }

    public void setAdded(int added) {
        this.added = added;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}
