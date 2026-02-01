package com.sma.core.entity;

import com.sma.core.enums.EvaluationStatus;
import com.sma.core.enums.MatchLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resume_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ai_overall_score")
    private Float aiOverallScore;

    @Column(name = "recruiter_overall_score")
    private Float recruiterOverallScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_level", columnDefinition = "match_level_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private MatchLevel matchLevel;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weakness;

    @Column(name = "is_true_level")
    private Boolean isTrueLevel;

    @Column(name = "has_related_experience")
    private Boolean hasRelatedExperience;

    @Column(name = "is_specific_jd")
    private Boolean isSpecificJd;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_status", columnDefinition = "evaluation_status_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EvaluationStatus evaluationStatus;

    @Column(name = "processing_time_second")
    private Float processingTimeSecond;

    @Column(name = "ai_model_version")
    private String aiModelVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EvaluationCriteriaScore> criteriaScores = new HashSet<>();

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EvaluationGap> gaps = new HashSet<>();

    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EvaluationWeakness> weaknesses = new HashSet<>();
}
