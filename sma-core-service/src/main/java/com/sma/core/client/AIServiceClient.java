package com.sma.core.client;

import com.sma.core.dto.message.suggest.ReSuggestRequestMessage;
import com.sma.core.dto.message.suggest.SuggestResultMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AIServiceClient {

    WebClient webClient;

    public AIServiceClient(
            WebClient.Builder builder,
            @Value("${app.ai-service}") String aiServer
    ) {
        this.webClient = builder
                .baseUrl(aiServer)
                .build();
    }

    public SuggestResultMessage reSuggestion(ReSuggestRequestMessage message) {
        return webClient.post()
                .uri("/v1/suggestion/re-generate")
                .bodyValue(message)
                .retrieve()
                .bodyToMono(SuggestResultMessage.class)
                .block();
    }

}
