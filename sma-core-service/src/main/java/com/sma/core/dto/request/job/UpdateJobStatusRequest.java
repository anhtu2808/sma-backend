package com.sma.core.dto.request.job;

import com.sma.core.enums.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateJobStatusRequest {

    @NotNull
    JobStatus jobStatus;

}
