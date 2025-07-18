package com.example.examine.entity.detail;

import com.example.examine.entity.Product;
import com.example.examine.entity.extend.EntityTime;
import com.example.examine.entity.Tag.Supplement;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supplement_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SupplementDetail extends EntityTime {
    @Id
    private Long supplementId; // 직접 ID 필드로 사용

    @OneToOne
    @MapsId // supplementId를 공유해서 primary key로 사용함
    @JoinColumn(name = "supplement_id")
    private Supplement supplement;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String overview;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String intro;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String positive;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String negative;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mechanism;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String dosage;

    @OneToMany(mappedBy = "supplementDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();
}
