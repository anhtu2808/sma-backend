package com.sma.core.dto.request.company;

import com.sma.core.enums.CompanyIndustry;
import com.sma.core.enums.CompanyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompanyRequest {

    String name;
    String description;
    CompanyIndustry companyindustry;
    CompanyType companyType;
    Integer minSize;
    Integer maxSize;
    @Email
    String companyEmail;
    String phone;
    String address;
    String district;
    String city;
    String country;
    String taxIdentificationNumber;
    String erc;
    String companyLink;

}
