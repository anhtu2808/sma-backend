package com.sma.core.config.RabbitMQProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq.proposed.cv")
@Data
public class ProposedCV {

    private String requestQueue = "proposed.cv.request";
    private String resultQueue = "proposed.cv.result";

}
