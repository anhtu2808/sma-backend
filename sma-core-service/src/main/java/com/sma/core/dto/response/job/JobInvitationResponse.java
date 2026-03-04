package com.sma.core.dto.response.job;

import com.sma.core.enums.Currency;
import com.sma.core.enums.JobLevel;
import com.sma.core.enums.WorkingModel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class JobInvitationResponse {

    Integer id;
    String name;
    Integer experienceTime;
    BigDecimal salaryStart;
    BigDecimal salaryEnd;
    Currency currency;
    JobLevel jobLevel;
    WorkingModel workingModel;

}
