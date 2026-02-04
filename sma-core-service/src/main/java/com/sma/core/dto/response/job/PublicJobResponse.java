package com.sma.core.dto.response.job;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class PublicJobResponse extends BaseJobResponse{

    String about;
    String responsibilities;
    String requirement;
}
