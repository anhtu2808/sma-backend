package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "evaluation_criteria_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationCriteriaScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scoring_criteria_id")
    private ScoringCriteria scoringCriteria;

    @Column(name = "max_score")
    @Builder.Default
    private Float maxScore = 100f;

    @Column(name = "ai_score")
    private Float aiScore;

    @Column(name = "manual_score")
    private Float manualScore;

    @Column(name = "weighted_score")
    private Float weightedScore;

    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(name = "manual_explanation", columnDefinition = "TEXT")
    private String manualExplanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id")
    private ResumeEvaluation evaluation;

    @OneToMany(mappedBy = "evaluationCriteriaScore", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EvaluationHardSkill> hardSkills = new HashSet<>();

    @OneToMany(mappedBy = "evaluationCriteriaScore", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EvaluationSoftSkill> softSkills = new HashSet<>();

    @OneToMany(mappedBy = "evaluationCriteriaScore", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EvaluationExperienceDetail> experienceDetails = new HashSet<>();
}
