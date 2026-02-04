package com.sma.core.dto.response.company;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class LocationShortResponse {
    String name;
    String address;
    String city;
    String district;
    String country;
    String googleMapLink;
}
