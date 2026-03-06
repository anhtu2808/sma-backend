package com.sma.core.config.RabbitMQProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq.re-suggest")
@Data
public class ReSuggestionRabbitMQProperties {

    private String requestQueue = "re.suggest.request";
    private String resultQueue = "re.suggest.result";

}
