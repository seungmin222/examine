package com.example.examine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    private String intro;
    private String positive;
    private String negative;
    private String mechanism;
    private String dosage;

}
