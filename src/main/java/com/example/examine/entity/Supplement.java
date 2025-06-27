package com.example.examine.entity;

import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.SupplementEffect.SupplementSideEffect;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "supplement", uniqueConstraints = @UniqueConstraint(columnNames = {"korName", "engName"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Supplement extends EntityTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true , nullable = false)
    private String korName;

    @Column(unique = true, nullable = false)
    private String engName;

    private BigDecimal dosageValue;

    private String dosageUnit;

    private BigDecimal cost;

    private LocalDateTime date;

    @ManyToMany
    @JoinTable(
            name = "supplement_type",
            joinColumns = @JoinColumn(name = "supplement_id"),
            inverseJoinColumns = @JoinColumn(name = "type_tag_id")
    )
    private List<TypeTag> types = new ArrayList<>();

    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementEffect> effects = new ArrayList<>();

    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementSideEffect> sideEffects = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "supplement_journal",
            joinColumns = @JoinColumn(name = "supplement_id"),
            inverseJoinColumns = @JoinColumn(name = "journal_id")
    )
    @JsonIgnore
    private List<Journal> journals = new ArrayList<>();

 // detail 클래스를 supplement 엔티티에 직접 추가하면 자원낭비
 // detail에서 id 받는게 적절

}
