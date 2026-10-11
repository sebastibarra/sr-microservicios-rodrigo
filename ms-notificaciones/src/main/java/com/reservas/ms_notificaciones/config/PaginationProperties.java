package com.reservas.ms_notificaciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.pagination")
public record PaginationProperties(
        @DefaultValue("10") int defaultSize,
        @DefaultValue("100") int maxSize
) {
}
