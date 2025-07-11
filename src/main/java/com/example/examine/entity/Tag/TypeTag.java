package com.example.examine.entity.Tag;

import com.example.examine.entity.SupplementType.SupplementType;
import com.example.examine.entity.extend.EntityTime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "type_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TypeTag extends EntityTime implements Tag{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String korName;

    @Column(unique = true)
    private String engName;

    @Builder.Default
    @Column(nullable = false)
    private String tier = "D";

    @Builder.Default
    @OneToMany(mappedBy = "typeTag", cascade = CascadeType.ALL)
    private List<SupplementType> st = new ArrayList<>();

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
