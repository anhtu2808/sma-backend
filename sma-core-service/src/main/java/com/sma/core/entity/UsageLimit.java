package com.sma.core.entity;

import com.sma.core.enums.UsageLimitUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Entity
@Table(name = "usage_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UsageLimit.UsageLimitId.class)
public class UsageLimit {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @Column(name = "max_quota", nullable = false)
    private Integer maxQuota;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_unit", nullable = false, columnDefinition = "usage_limit_unit")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UsageLimitUnit limitUnit;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageLimitId implements Serializable {
        private Integer plan;
        private Integer feature;
    }
}
