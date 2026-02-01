package com.sma.core.entity;

import com.sma.core.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private Package packageEntity;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "subscription_status_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.PENDING_PAYMENT;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    @Column(name = "renewed_at")
    private LocalDateTime renewedAt;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PaymentHistory> paymentHistories = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        purchasedAt = LocalDateTime.now();
    }
}
