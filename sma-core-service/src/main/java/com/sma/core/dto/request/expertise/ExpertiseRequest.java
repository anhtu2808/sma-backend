package com.sma.core.dto.request.expertise;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertiseRequest {
    @NotBlank(message = "Expertise name is required")
    String name;
    String description;
    @NotNull(message = "Group ID is required")
    Integer groupId;
}
