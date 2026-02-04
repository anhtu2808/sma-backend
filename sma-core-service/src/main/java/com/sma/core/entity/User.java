package com.sma.core.entity;

import com.sma.core.enums.Gender;
import com.sma.core.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "user_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserStatus status;

    @Column(name = "full_name")
    private String fullName;

    private String avatar;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", columnDefinition = "gender_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Candidate candidate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Recruiter recruiter;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserToken> tokens = new HashSet<>();
}
