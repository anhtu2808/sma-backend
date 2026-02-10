package com.sma.core.entity;

import com.sma.core.enums.PlanDurationUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "plan_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "original_price", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal originalPrice = BigDecimal.ZERO;

    @Column(name = "sale_price", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal salePrice = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(nullable = false)
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "plan_duration_unit")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PlanDurationUnit unit;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
