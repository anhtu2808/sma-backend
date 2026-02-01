package com.sma.core.entity;

import com.sma.core.enums.CriteriaType;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type")
    private CriteriaType criteriaType;

    @Column(name = "default_weight")
    private Double defaultWeight;
}
