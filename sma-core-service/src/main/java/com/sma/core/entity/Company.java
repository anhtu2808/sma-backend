package com.sma.core.entity;

import com.sma.core.enums.CompanyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String country;

    @Column(name = "company_industry")
    private String companyIndustry;

    @Column(name = "min_size")
    private String minSize;
    @Column(name= "max_size")
    private String maxSize;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String link;

    @Column(name = "follower_number")
    private Integer followerNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "company_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CompanyStatus status;

    @Column(name = "type")
    private String companyType;

    private String logo;

    @Column(name = "tax_identification_number")
    private String taxIdentificationNumber;

    private String email;

    private String phone;

    @Column(name = "sign_commitment")
    private Boolean signCommitment;

    private String erc;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Recruiter> recruiters = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Job> jobs = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CompanyLocation> locations = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CompanyImage> images = new HashSet<>();
}
