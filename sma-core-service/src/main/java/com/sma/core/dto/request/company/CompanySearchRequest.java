package com.sma.core.dto.request.company;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CompanySearchRequest {

    String name;
    Set<String> location;

    @Builder.Default
    Integer page = 0;

    @Builder.Default
    Integer size = 10;

}
