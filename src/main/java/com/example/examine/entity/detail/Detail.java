package com.example.examine.entity.detail;

import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Tag;

public interface Detail {
    Long getId();
    String getOverview();
    void setOverview(String overview);
    String getIntro();
    void setIntro(String intro);
    Tag getTag();
    void setTag(Tag tag);
}
