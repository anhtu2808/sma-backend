package com.sma.core.dto.response.job;

import com.sma.core.dto.response.company.CompanyDetailResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class JobInternalResponse extends CompanyDetailResponse {

    Boolean isViolated;
    Integer quantity;
    Double autoRejectThreshold;
    JobInternalResponse rootJob;
}
