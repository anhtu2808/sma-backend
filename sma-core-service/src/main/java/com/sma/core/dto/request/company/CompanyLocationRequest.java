package com.sma.core.dto.request.company;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyLocationRequest {
    private Integer id;
    private String name;
    private String address;
    private String district;
    private String city;
    private String country;
    private String description;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String googleMapLink;
}
