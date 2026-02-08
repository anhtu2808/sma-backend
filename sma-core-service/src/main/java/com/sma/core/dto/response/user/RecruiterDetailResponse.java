package com.sma.core.dto.response.user;

import com.sma.core.dto.response.job.BaseJobResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RecruiterDetailResponse {
    Boolean isRootRecruiter;
    Boolean isVerified;
    String companyName;
    String companyLogo;
    List<BaseJobResponse> jobs;
}
