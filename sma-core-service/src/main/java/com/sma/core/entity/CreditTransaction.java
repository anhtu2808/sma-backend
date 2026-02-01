package com.sma.core.entity;

import com.sma.core.enums.CreditType;
import com.sma.core.enums.ReferenceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "credit_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_wallet_id", nullable = false)
    private CreditWallet creditWallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_type", nullable = false, columnDefinition = "credit_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CreditType creditType;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", columnDefinition = "reference_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ReferenceType referenceType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
