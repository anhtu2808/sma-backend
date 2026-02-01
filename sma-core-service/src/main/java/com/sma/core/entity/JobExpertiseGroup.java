package com.sma.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_expertise_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExpertiseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
