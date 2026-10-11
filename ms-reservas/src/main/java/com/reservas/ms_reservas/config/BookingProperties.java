package com.reservas.ms_reservas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(
        @DefaultValue("24") int minAdvanceHours,
        @DefaultValue("min_advance_hours") String minAdvanceSettingKey,
        @DefaultValue("") String zoneId
) {
}
