package com.sma.core.entity;

import com.sma.core.enums.ExportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "export_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exported_by", nullable = false)
    private User exportedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_type", nullable = false, columnDefinition = "export_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ExportType exportType;

    @Column(name = "candidate_count", nullable = false)
    @Builder.Default
    private Integer candidateCount = 0;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
