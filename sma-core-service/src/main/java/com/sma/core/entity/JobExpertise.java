package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @OneToMany(mappedBy = "expertise", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Job> jobs = new HashSet<>();
}
