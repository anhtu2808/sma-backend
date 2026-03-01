package com.sma.core.entity;

import com.sma.core.enums.EventSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usage_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_source", length = 50)
    private EventSource eventSource;

    @Column(name = "source_id")
    private Integer sourceId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
