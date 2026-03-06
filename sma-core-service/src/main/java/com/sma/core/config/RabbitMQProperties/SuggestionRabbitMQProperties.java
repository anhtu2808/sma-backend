package com.sma.core.config.RabbitMQProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq.suggest")
@Data
public class SuggestionRabbitMQProperties {
    private String requestQueue = "suggest.request";
    private String resultQueue = "suggest.result";
}
