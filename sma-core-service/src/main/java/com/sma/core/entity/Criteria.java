package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "criterias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Criteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String rule;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "default_weight")
    private Double weight;

    @Builder.Default
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

}
