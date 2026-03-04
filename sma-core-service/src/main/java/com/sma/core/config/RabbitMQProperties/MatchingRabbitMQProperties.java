package com.sma.core.config.RabbitMQProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq.matching")
@Data
public class MatchingRabbitMQProperties {

    private String requestQueue = "resume.matching.request";
    private String resultQueue = "resume.matching.result";

}
