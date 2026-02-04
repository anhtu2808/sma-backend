package com.sma.core.dto.response.company;

import com.sma.core.entity.Recruiter;
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
public class CompanyInternalResponse extends CompanyDetailResponse {

    String taxIdentificationNumber;
    Boolean signCommitment;
    String erc;
    Set<Recruiter> recruiters = new HashSet<>();

}
