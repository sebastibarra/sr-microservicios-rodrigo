package com.reservas.ms_reservas;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Punto de entrada cuando el servicio se despliega como WAR en un Tomcat externo.
 *
 * <p>La configuración ({@code DB_URL}, {@code JWT_SECRET}, etc.) llega como parámetros del
 * contexto de Tomcat ({@code conf/<servicio>/localhost/ROOT.xml}), que Spring lee como propiedades.</p>
 */
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(MsReservasApplication.class);
	}
}
