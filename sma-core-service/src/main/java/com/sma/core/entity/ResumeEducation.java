package com.sma.core.entity;

import com.sma.core.enums.DegreeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "resume_educations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String institution;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree", columnDefinition = "degree_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DegreeType degree;

    @Column(name = "major_field")
    private String majorField;

    private Double gpa;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    private Boolean isCurrent;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;
}
