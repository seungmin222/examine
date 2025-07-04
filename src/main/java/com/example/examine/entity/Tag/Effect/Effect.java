package com.example.examine.entity.Tag.Effect;

import com.example.examine.entity.SupplementEffect.SE;

import java.util.List;

public interface Effect  {
    Long getId();
    String getKorName();
    void setKorName(String korName);
    String getEngName();
    void setEngName(String engName);
    List<SE> getSE();
    void setSE(List<SE> se);
}
