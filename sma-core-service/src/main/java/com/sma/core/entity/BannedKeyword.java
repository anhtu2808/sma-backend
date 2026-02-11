package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "banned_keywords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannedKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String word;

    @Column(name = "is_active")
    private Boolean isActive;
}
