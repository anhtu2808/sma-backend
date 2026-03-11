package com.sma.core.dto.message.proposed;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProposedCVResultMessage {

    Integer jobId;
    List<ProposedCVData> proposedCVs;

}
