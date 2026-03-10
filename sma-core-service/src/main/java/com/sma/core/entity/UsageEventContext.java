package com.sma.core.entity;

import com.sma.core.enums.EventSource;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "usage_event_contexts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_usage_event_contexts_event_source_id",
                        columnNames = {"usage_event_id", "event_source", "source_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageEventContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usage_event_id", nullable = false)
    private UsageEvent usageEvent;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_source", nullable = false, length = 50)
    private EventSource eventSource;

    @Column(name = "source_id", nullable = false)
    private Integer sourceId;
}
