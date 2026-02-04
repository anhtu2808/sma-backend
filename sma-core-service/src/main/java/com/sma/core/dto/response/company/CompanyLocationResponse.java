package com.sma.core.dto.response.company;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CompanyLocationResponse {
    Integer id;
    String name;
    String address;
    String district;
    String city;
    String country;
    String description;
    BigDecimal longitude;
    BigDecimal latitude;
    String googleMapLink;

}
