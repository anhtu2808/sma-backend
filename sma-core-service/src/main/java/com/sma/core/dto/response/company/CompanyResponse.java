package com.sma.core.dto.response.company;

import com.sma.core.dto.response.recruiter.RecruiterShortResponse;
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
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CompanyResponse {
//RESPONSE FOR ADMIN AND RECRUITER
    Integer id;
    String name;
    String country;
    String companyIndustry;
    Integer minSize;
    Integer maxSize;
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
    String rejectReason;
    List<RecruiterShortResponse> recruiters;
    List<LocationShortResponse> locations;
    List<String> images;
    long totalJobs;
}
