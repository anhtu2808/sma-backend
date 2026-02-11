package com.sma.core.entity;

import com.sma.core.enums.PlanTarget;
import com.sma.core.enums.PlanType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "plan_details", columnDefinition = "TEXT")
    private String planDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_target", nullable = false, columnDefinition = "plan_target_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PlanTarget planTarget;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, columnDefinition = "plan_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PlanType planType;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_popular", nullable = false)
    @Builder.Default
    private Boolean isPopular = false;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlanPrice> planPrices = new HashSet<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UsageLimit> usageLimits = new HashSet<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Subscription> subscriptions = new HashSet<>();
}
