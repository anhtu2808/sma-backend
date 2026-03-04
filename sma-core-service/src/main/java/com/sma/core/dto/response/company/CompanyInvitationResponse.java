package com.sma.core.dto.response.company;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CompanyInvitationResponse {

    Integer id;
    String name;
    String industry;
    String logo;
    String email;
    List<LocationShortResponse> locations;

}
