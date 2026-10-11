package com.reservas.ms_notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MsNotificacionesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsNotificacionesApplication.class, args);
	}

}
