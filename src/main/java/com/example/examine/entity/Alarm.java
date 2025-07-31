package com.example.examine.entity;

import com.example.examine.entity.extend.EntityTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alarm")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alarm extends EntityTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = true)
    private Page page;

}

