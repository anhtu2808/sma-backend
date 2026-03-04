package com.sma.core.config.RabbitMQProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq.criteria-context")
@Data
public class CriteriaContextRabbitMQProperties {

    private String requestQueue = "criteria.context.request";
    private String resultQueue = "criteria.context.result";

}
