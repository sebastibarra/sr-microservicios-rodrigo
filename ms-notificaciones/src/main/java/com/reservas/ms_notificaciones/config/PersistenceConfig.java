package com.reservas.ms_notificaciones.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = "com.reservas.commons.entity")
public class PersistenceConfig {
}
