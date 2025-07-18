package com.example.examine.entity;

import com.example.examine.entity.User.UserProduct;
import com.example.examine.entity.detail.SupplementDetail;
import com.example.examine.entity.extend.EntityTime;
import com.example.examine.service.util.EnumService;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Product extends EntityTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String link;
    private String imageUrl;

    private BigDecimal dosageValue;
    private EnumService.DosageUnit dosageUnit;

    private BigDecimal price;         // 총 가격
    private BigDecimal pricePerDose;  // 복용량당 가격 (옵션: 계산 후 저장)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplement_detail_id")
    private SupplementDetail supplementDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = true) // optional=true가 nullable 의미
    @JoinColumn(name = "brand_id", nullable = true)     // nullable=true도 함께 명시 (선택사항)
    private Brand brand;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<UserProduct> users = new ArrayList<>();

    public String getDosageUnit() {
        return dosageUnit != null ? dosageUnit.name().toLowerCase() : null;
    }

    public void setDosageUnit(String str) {
        this.dosageUnit = EnumService.DosageUnit.fromString(str);
    }
}
