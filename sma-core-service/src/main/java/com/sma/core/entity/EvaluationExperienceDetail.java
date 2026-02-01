package com.sma.core.entity;

import com.sma.core.enums.RelevanceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "evaluation_experience_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationExperienceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_name")
    private String companyName;

    private String position;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "key_achievements", columnDefinition = "TEXT")
    private String keyAchievements;

    @Column(name = "technologies_used", columnDefinition = "TEXT")
    private String technologiesUsed;

    @Column(name = "is_relevant")
    private Boolean isRelevant;

    @Enumerated(EnumType.STRING)
    @Column(name = "transferability_to_role", columnDefinition = "transferability_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private RelevanceType transferabilityToRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_gravity", columnDefinition = "experience_gravity_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private RelevanceType experienceGravity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_criteria_score_id")
    private EvaluationCriteriaScore evaluationCriteriaScore;
}
