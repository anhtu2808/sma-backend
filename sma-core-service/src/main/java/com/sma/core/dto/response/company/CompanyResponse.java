package com.sma.core.dto.response.company;

import com.sma.core.entity.CompanyImage;
import com.sma.core.entity.CompanyLocation;
import com.sma.core.entity.Job;
import com.sma.core.entity.Recruiter;
import com.sma.core.enums.CompanyStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CompanyResponse {

    Integer id;
    String name;
    String country;
    String companyIndustry;
    String size;
    String description;
    String link;
    Integer followerNumber;
    CompanyStatus status;
    String companyType;
    String logo;
    String taxIdentificationNumber;
    String email;
    String phone;
    Boolean signCommitment;
    String erc;
//    Set<Recruiter> recruiters = new HashSet<>();
//    Set<Job> jobs = new HashSet<>();
//    Set<CompanyLocation> locations = new HashSet<>();
//    Set<CompanyImage> images = new HashSet<>();
}
