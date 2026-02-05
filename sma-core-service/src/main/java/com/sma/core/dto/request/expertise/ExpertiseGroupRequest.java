package com.sma.core.dto.request.expertise;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertiseGroupRequest {
    @NotBlank(message = "Expertise group name is required")
    String name;
    String description;
}
