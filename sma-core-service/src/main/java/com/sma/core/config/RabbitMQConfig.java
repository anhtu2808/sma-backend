package com.sma.core.config;

import com.sma.core.config.RabbitMQProperties.*;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class RabbitMQConfig {
    private final ResumeParsingRabbitMQProperties resumeParsingRabbitMQProperties;
    private final MatchingRabbitMQProperties matchingRabbitMQProperties;
    private final CriteriaContextRabbitMQProperties criteriaContextRabbitMQProperties;
    private final SuggestionRabbitMQProperties suggestionRabbitMQProperties;
    private final ReSuggestionRabbitMQProperties reSuggestionRabbitMQProperties;
    private final EmbeddingResume embeddingResume;
    private final EmbeddingJob embeddingJob;
    private final ProposedCV proposedCV;
    @Bean
    public Queue resumeParsingRequestQueue() {
        return QueueBuilder
                .durable(resumeParsingRabbitMQProperties.getRequestQueue())
                .build();
    }

    @Bean
    public Queue resumeParsingResultQueue() {
        return QueueBuilder
                .durable(resumeParsingRabbitMQProperties.getResultQueue())
                .build();
    }

    @Bean
    public Queue matchingRequestQueue() {
        return QueueBuilder
                .durable(matchingRabbitMQProperties.getRequestQueue())
                .build();
    }

    @Bean
    public Queue matchingResultQueue() {
        return QueueBuilder
                .durable(matchingRabbitMQProperties.getResultQueue())
                .build();
    }

    @Bean
    public Queue criteriaContextRequestQueue() {
        return QueueBuilder
                .durable(criteriaContextRabbitMQProperties.getRequestQueue())
                .build();
    }

    @Bean
    public Queue criteriaContextResultQueue() {
        return QueueBuilder
                .durable(criteriaContextRabbitMQProperties.getResultQueue())
                .build();
    }

    @Bean
    public Queue suggestRequestQueue() {
        return QueueBuilder
                .durable(suggestionRabbitMQProperties.getRequestQueue())
                .build();
    }

    @Bean
    public Queue suggestResultQueue() {
        return QueueBuilder
                .durable(suggestionRabbitMQProperties.getResultQueue())
                .build();
    }

    @Bean
    public Queue reSuggestRequestQueue() {
        return QueueBuilder
                .durable(reSuggestionRabbitMQProperties.getRequestQueue())
                .build();
    }

    @Bean
    public Queue reSuggestResultQueue() {
        return QueueBuilder
                .durable(reSuggestionRabbitMQProperties.getResultQueue())
                .build();
    }

    @Bean
    public Queue embeddingResumeRequestQueue() {
        return QueueBuilder
                .durable(embeddingResume.getRequestQueue())
                .build();
    }

    @Bean
    public Queue embeddingResumeResultQueue() {
        return QueueBuilder
                .durable(embeddingResume.getResultQueue())
                .build();
    }

    @Bean
    public Queue embeddingJobRequestQueue() {
        return QueueBuilder
                .durable(embeddingJob.getRequestQueue())
                .build();
    }

    @Bean
    public Queue embeddingJobResultQueue() {
        return QueueBuilder
                .durable(embeddingJob.getResultQueue())
                .build();
    }

    @Bean
    public Queue proposedCVRequestQueue() {
        return QueueBuilder
                .durable(proposedCV.getRequestQueue())
                .build();
    }

    @Bean
    public Queue proposedCVResultQueue() {
        return QueueBuilder
                .durable(proposedCV.getResultQueue())
                .build();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
