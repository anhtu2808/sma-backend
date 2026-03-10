package com.sma.core.config.RabbitMQProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq.embedding.job")
@Data
public class EmbeddingJob {

    private String requestQueue = "embedding.job.request";
    private String resultQueue = "embedding.job.result";

}
