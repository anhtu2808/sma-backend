package com.sma.core.dto.message.embedding;

import com.sma.core.enums.EmbedStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmbeddingResultMessage {

    Integer id;
    EmbedStatus status;
    String errorMessage;

}
