package com.sma.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${springdoc.swagger-ui.server-url}")
    private String swaggerServerUrl;

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String API_KEY_SCHEME = "apiKey";

    @Bean
    public OpenAPI customOpenAPI() {
        // Define server
        Server apiServer = new Server();
        apiServer.setUrl(swaggerServerUrl);
        apiServer.setDescription("API Server");

        // Define security scheme
        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityScheme apiKeySchema = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER);

        // Apply security scheme globally
        return new OpenAPI()
                .servers(List.of(apiServer))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerAuthScheme)
                        .addSecuritySchemes(API_KEY_SCHEME, apiKeySchema))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME));
    }
}
