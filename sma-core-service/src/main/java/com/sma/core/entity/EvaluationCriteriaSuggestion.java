package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluation_criteria_suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationCriteriaSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String suggestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_criteria_detail_id")
    private EvaluationCriteriaDetail evaluationCriteriaDetail;

}
