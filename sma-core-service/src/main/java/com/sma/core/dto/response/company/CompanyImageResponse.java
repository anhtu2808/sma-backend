package com.sma.core.dto.response.company;

import com.sma.core.entity.Company;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CompanyImageResponse {

    Integer id;
    String url;
    String description;

}
