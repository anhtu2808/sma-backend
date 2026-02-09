package com.sma.core.entity;

import com.sma.core.enums.EmploymentType;
import com.sma.core.enums.WorkingModel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resume_experiences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String company;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    private Boolean isCurrent;

    @Enumerated(EnumType.STRING)
    @Column(name = "working_model", columnDefinition = "working_model_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private WorkingModel workingModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", columnDefinition = "employment_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EmploymentType employmentType;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @OneToMany(mappedBy = "experience", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ResumeExperienceDetail> details = new HashSet<>();
}
