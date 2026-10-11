package com.reservas.ms_notificaciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.mail")
public record MailSettings(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("no-reply@reservas.local") String from,
        @DefaultValue("soporte@reservas.local") String supportAddress
) {
}
