package com.example.examine.entity.Effect;

import com.example.examine.entity.SupplementEffect.SE;

import java.util.List;

public interface Effect {
    Long getId();
    String getName();
    void setName(String name);
    List<SE> getSE();
    void setSE(List<SE> se);
}
