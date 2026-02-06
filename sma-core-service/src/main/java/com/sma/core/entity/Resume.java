package com.sma.core.entity;

import com.sma.core.enums.ResumeLanguage;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.enums.ResumeStatus;
import com.sma.core.enums.ResumeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "resume_name")
    private String resumeName;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "address_in_resume")
    private String addressInResume;

    @Column(name = "phone_in_resume")
    private String phoneInResume;

    @Column(name = "email_in_resume")
    private String emailInResume;

    @Column(name = "github_link")
    private String githubLink;

    @Column(name = "linkedin_link")
    private String linkedinLink;

    @Column(name = "portfolio_link")
    private String portfolioLink;

    @Column(name = "full_name")
    private String fullName;

    private String avatar;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "resume_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ResumeType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_resume_id")
    private Resume rootResume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "resume_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ResumeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", columnDefinition = "parse_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ResumeParseStatus parseStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", columnDefinition = "resume_language")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ResumeLanguage language;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "is_overrided")
    private Boolean isOverrided;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ResumeSkill> skills = new HashSet<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ResumeEducation> educations = new HashSet<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ResumeExperience> experiences = new HashSet<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ResumeProject> projects = new HashSet<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ResumeCertification> certifications = new HashSet<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ResumeEvaluation> evaluations = new HashSet<>();
}
