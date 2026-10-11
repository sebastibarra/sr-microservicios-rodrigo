package com.reservas.ms_reservas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(BookingProperties booking) {
        ZoneId zone = StringUtils.hasText(booking.zoneId()) ? ZoneId.of(booking.zoneId()) : ZoneId.systemDefault();
        return Clock.system(zone);
    }
}
