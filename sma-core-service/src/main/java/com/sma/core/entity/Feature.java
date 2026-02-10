package com.sma.core.entity;

import com.sma.core.enums.UsageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", columnDefinition = "feature_usage_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UsageType usageType;

    @Column(name = "feature_key", nullable = false, unique = true, length = 100)
    private String featureKey;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
