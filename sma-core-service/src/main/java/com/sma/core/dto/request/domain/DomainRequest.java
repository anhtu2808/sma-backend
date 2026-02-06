package com.sma.core.dto.request.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DomainRequest {
    @NotBlank(message = "Domain name is required")
    String name;
    String description;
}
