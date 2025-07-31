package com.example.examine.entity.Tag;

import com.example.examine.entity.Product;
import com.example.examine.entity.extend.EntityTime;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Brand extends EntityTime implements Tag{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true , nullable = false)
    private String korName;

    @Column(unique = true, nullable = false)
    private String engName;

    private String country;

    private String fei;

    private int nai; // No Action
    private int vai; // Voluntary Action
    private int oai; // Official Action

    private double score;

    private String tier; // "A", "B", ...

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getKorName() {
        return korName;
    }

    @Override
    public void setKorName(String korName) {
        this.korName = korName;
    }

    @Override
    public String getEngName() {
        return engName;
    }

    @Override
    public void setEngName(String engName) {
        this.engName = engName;
    }

    @Override
    public String getTier() {
        return tier;
    }

    @Override
    public void setTier(String tier) {
        this.tier = tier;
    }
}
