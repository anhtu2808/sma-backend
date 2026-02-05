package com.sma.core.dto.request.company;

import com.sma.core.enums.CompanyStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyVerificationRequest {
    CompanyStatus status;
    String reason;
}
