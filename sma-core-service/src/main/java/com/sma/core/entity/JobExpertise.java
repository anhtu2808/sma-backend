package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_expertises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExpertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertise_group_id")
    private JobExpertiseGroup expertiseGroup;
}
