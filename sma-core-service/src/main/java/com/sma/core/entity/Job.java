package com.sma.core.entity;

import com.sma.core.enums.JobLevel;
import com.sma.core.enums.JobStatus;
import com.sma.core.enums.WorkingModel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @Column(columnDefinition = "TEXT")
    private String requirement;

    @Column(name = "is_violated")
    private Boolean isViolated;

    @Column(name = "enable_ai_scoring")
    private Boolean enableAiScoring;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Column(name = "exp_date")
    private LocalDateTime expDate;

    @Column(name = "salary_start", precision = 15, scale = 2)
    private BigDecimal salaryStart;

    @Column(name = "salary_end", precision = 15, scale = 2)
    private BigDecimal salaryEnd;

    @Column(length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "experience_time")
    private Integer experienceTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "job_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private JobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_level", columnDefinition = "job_level_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private JobLevel jobLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "working_model", columnDefinition = "working_model_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private WorkingModel workingModel;

    private Integer quantity;

    @Column(name = "auto_reject_threshold")
    private Double autoRejectThreshold;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_id")
    private Job rootJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertise_id")
    private JobExpertise expertise;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_domains", joinColumns = @JoinColumn(name = "job_id"), inverseJoinColumns = @JoinColumn(name = "domain_id"))
    @Builder.Default
    private Set<Domain> domains = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_benefits", joinColumns = @JoinColumn(name = "job_id"), inverseJoinColumns = @JoinColumn(name = "benefit_id"))
    @Builder.Default
    private Set<Benefit> benefits = new HashSet<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Application> applications = new HashSet<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<JobQuestion> questions = new HashSet<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ScoringCriteria> scoringCriterias = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_locations", joinColumns = @JoinColumn(name = "job_id"), inverseJoinColumns = @JoinColumn(name = "company_location_id"))
    @Builder.Default
    private Set<CompanyLocation> locations = new HashSet<>();
}
