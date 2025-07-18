package com.example.examine.entity;

import com.example.examine.entity.extend.EntityTime;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Brand extends EntityTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

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
}
