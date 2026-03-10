package com.sma.core.entity;

import com.sma.core.enums.LabelStatus;
import com.sma.core.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "evaluation_criteria_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationCriteriaDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_status", columnDefinition = "label_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private LabelStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_level", columnDefinition = "skill_level_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SkillLevel requiredLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "candidate_level", columnDefinition = "skill_level_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SkillLevel candidateLevel;

    private Boolean isRequired;
    private Integer startIndex;
    private Integer endIndex;
    @Builder.Default
    private Boolean isFixed = false;
    private Float impactScore;

    @OneToMany(mappedBy = "evaluationCriteriaDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EvaluationCriteriaSuggestion> suggestions = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_criteria_score_id")
    private EvaluationCriteriaScore evaluationCriteriaScore;

}
