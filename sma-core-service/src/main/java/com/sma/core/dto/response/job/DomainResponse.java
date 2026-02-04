package com.sma.core.dto.response.job;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class DomainResponse {
    Integer id;
    String name;
    String description;
}
