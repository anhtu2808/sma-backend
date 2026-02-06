package com.sma.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq.resume-parsing")
@Data
public class ResumeParsingRabbitMQProperties {
    private String requestQueue = "resume.parsing.request";
    private String resultQueue = "resume.parsing.result";
}
