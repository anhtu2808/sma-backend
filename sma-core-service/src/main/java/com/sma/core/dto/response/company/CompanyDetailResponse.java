package com.sma.core.dto.response.company;

import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.entity.CompanyImage;
import com.sma.core.entity.CompanyLocation;
import com.sma.core.entity.Job;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class CompanyDetailResponse extends BaseCompanyResponse {

    String size;
    String description;
    String link;
    Integer followerNumber;
    String companyType;
    String email;
    String phone;
    Set<BaseJobResponse> jobs;
    Set<CompanyLocationResponse> locations;
    Set<CompanyImageResponse> images;

}
