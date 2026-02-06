package com.sma.core.dto.response.company;

import com.sma.core.dto.response.recruiter.RecruiterShortResponse;
import com.sma.core.entity.Recruiter;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class CompanyDetailResponse extends BaseCompanyResponse {

    Integer minSize;
    Integer maxSize;
    String description;
    String link;
    Integer followerNumber;
    String companyType;
    String email;
    String phone;
    Set<RecruiterShortResponse> recruiters;
    Set<CompanyLocationResponse> locations;
    Set<CompanyImageResponse> images;
    String taxIdentificationNumber;
    Boolean signCommitment;
    String erc;
    long totalJobs;

}
