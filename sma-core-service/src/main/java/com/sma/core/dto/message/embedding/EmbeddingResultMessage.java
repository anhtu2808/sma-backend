package com.sma.core.dto.message.embedding;

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
    String status;
    String errorMessage;

}
