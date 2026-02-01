package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "package_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PackageFeature.PackageFeatureId.class)
public class PackageFeature {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private Package packageEntity;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @Column(name = "max_quota")
    @Builder.Default
    private Integer maxQuota = 0;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageFeatureId implements Serializable {
        private Integer packageEntity;
        private Integer feature;
    }
}
