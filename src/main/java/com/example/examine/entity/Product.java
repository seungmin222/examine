package com.example.examine.entity;

import com.example.examine.entity.Tag.Brand;
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
@Table(
        name = "product",
        uniqueConstraints = @UniqueConstraint(columnNames = {"site_type", "site_product_id"})
)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "site_type", nullable = false, columnDefinition = "VARCHAR(50)")
    private EnumService.ProductSiteType siteType;

    @Column(name = "site_product_id", nullable = false)
    private String siteProductId;

    private BigDecimal dosageValue;

    @Enumerated(EnumType.STRING)
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

}
