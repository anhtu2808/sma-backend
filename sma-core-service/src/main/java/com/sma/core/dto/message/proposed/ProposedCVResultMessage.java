package com.sma.core.dto.message.proposed;

import com.sma.core.enums.EmbedStatus;
import com.sma.core.enums.ProposeStatus;
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

    ProposeStatus status;
    String errorMessage;
    Integer jobId;
    List<ProposedCVData> proposedCVs;

}
