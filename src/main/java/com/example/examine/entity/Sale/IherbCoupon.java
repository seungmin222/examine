package com.example.examine.entity.Sale;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "iherb_coupon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IherbCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String detail; // alt 설명

    @Column(name = "promo_code", length = 50)
    private String promoCode; // 할인 코드

    @Column(length = 500)
    private String link; // 쇼핑 링크

    private LocalDateTime expiresAt; // 종료 시각 (nullable)

    @CreationTimestamp
    private LocalDateTime createdAt;
}
