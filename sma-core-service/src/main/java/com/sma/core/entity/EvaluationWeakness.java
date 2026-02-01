package com.sma.core.entity;

import com.sma.core.enums.CriteriaType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluation_weaknesses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationWeakness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "weakness_text", columnDefinition = "TEXT")
    private String weaknessText;

    @Column(columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "start_index")
    private Integer startIndex;

    @Column(name = "end_index")
    private Integer endIndex;

    private String context;

    @Enumerated(EnumType.STRING)
    @Column(name = "criterion_type")
    private CriteriaType criterionType;

    private Short severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id")
    private ResumeEvaluation evaluation;
}
