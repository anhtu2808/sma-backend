package com.sma.core.entity;

import com.sma.core.enums.RelevanceType;
import com.sma.core.enums.SkillCategory;
import com.sma.core.enums.SkillLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "evaluation_hard_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationHardSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "skill_name")
    private String skillName;

    @Column(columnDefinition = "TEXT")
    private String evidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_category", columnDefinition = "skill_category_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SkillCategory skillCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_level", columnDefinition = "skill_level_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SkillLevel requiredLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "candidate_level", columnDefinition = "skill_level_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SkillLevel candidateLevel;

    @Column(name = "match_score")
    private Float matchScore;

    @Column(name = "years_of_experience")
    private Float yearsOfExperience;

    @Column(name = "is_critical")
    private Boolean isCritical;

    @Column(name = "is_matched")
    private Boolean isMatched;

    @Column(name = "is_missing")
    private Boolean isMissing;

    @Column(name = "is_extra")
    private Boolean isExtra;

    @Enumerated(EnumType.STRING)
    @Column(name = "relevance", columnDefinition = "relevance_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private RelevanceType relevance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_criteria_score_id")
    private EvaluationCriteriaScore evaluationCriteriaScore;
}
