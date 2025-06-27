package com.example.examine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "page")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Page extends EntityTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 512, nullable = false, unique = true)
    private String link;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "bookmark_count", nullable = false)
    private Long bookmarkCount = 0L;

    @Column(name = "level", nullable = false)
    private int level = 0;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPage> userPages = new ArrayList<>();
}
