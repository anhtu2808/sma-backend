package com.sma.core.dto.message.suggest;

import com.sma.core.dto.response.suggestion.GapSuggestionResponse;
import com.sma.core.dto.response.suggestion.WeaknessSuggestionResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuggestResultMessage {

    Integer evaluationId;
    String status;
    String errorMessage;
    Set<WeaknessSuggestionResponse> weaknessSuggestion;
    Set<GapSuggestionResponse> gapSuggestion;

}
