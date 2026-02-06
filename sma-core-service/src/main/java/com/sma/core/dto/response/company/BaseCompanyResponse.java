package com.sma.core.dto.response.company;

import com.sma.core.enums.CompanyStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class BaseCompanyResponse {

    Integer id;
    String name;
    String country;
    String companyIndustry;
    String link;
    Integer followerNumber;
    CompanyStatus companyStatus;
    String logo;
    Integer recruiterCount;

}
