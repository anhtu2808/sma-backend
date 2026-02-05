package com.sma.core.dto.response.company;

import com.sma.core.enums.CompanyStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AdminCompanyResponse {
    Integer id;
    String name;
    String logo;
    String email;
    String phone;
    String taxIdentificationNumber;
    String erc;
    CompanyStatus status;
    String rejectReason;
    String country;
    long recruiterCount;
    LocalDateTime createdAt;
}
