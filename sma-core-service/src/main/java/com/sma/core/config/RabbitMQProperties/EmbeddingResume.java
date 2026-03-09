package com.sma.core.config.RabbitMQProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq.embedding.resume")
@Data
public class EmbeddingResume {

    private String requestQueue = "embedding.resume.request";
    private String resultQueue = "embedding.resume.result";

}
