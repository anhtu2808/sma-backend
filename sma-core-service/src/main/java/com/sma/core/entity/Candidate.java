package com.sma.core.entity;

import com.sma.core.enums.JobSearchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "expected_salary_min", precision = 15, scale = 2)
    private BigDecimal expectedSalaryMin;

    @Column(name = "expected_salary_max", precision = 15, scale = 2)
    private BigDecimal expectedSalaryMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_search_status")
    @Builder.Default
    private JobSearchStatus jobSearchStatus = JobSearchStatus.OPEN_TO_OFFERS;

    @Column(name = "availability_date")
    private LocalDate availabilityDate;

    @Column(name = "is_profile_public")
    @Builder.Default
    private Boolean isProfilePublic = true;

    @Column(name = "profile_completeness")
    @Builder.Default
    private Integer profileCompleteness = 0;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Resume> resumes = new HashSet<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Application> applications = new HashSet<>();
}
