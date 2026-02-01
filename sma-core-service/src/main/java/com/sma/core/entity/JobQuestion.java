package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "job_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @OneToMany(mappedBy = "jobQuestion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<JobAnswer> answers = new HashSet<>();
}
