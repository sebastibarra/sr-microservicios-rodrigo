package com.reservas.ms_notificaciones.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificacionesOpenApi(
            @Value("${spring.application.name}") String name,
            @Value("${app.openapi.description:Notifications API}") String description,
            @Value("${app.openapi.version:1.0.0}") String version
    ) {
        return new OpenAPI().info(new Info().title(name).description(description).version(version));
    }
}
