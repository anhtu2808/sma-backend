package com.sma.core.entity;

import com.sma.core.enums.GapType;
import com.sma.core.enums.ImpactType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluation_gaps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationGap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "gap_type")
    private GapType gapType;

    @Column(name = "item_name")
    private String itemName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ImpactType impact;

    @Column(name = "impact_score")
    private Float impactScore;

    @Column(columnDefinition = "TEXT")
    private String suggestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id")
    private ResumeEvaluation evaluation;
}
