package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluation_soft_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationSoftSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "skill_name")
    private String skillName;

    @Column(columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "is_found")
    private Boolean isFound;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_criteria_score_id")
    private EvaluationCriteriaScore evaluationCriteriaScore;
}
