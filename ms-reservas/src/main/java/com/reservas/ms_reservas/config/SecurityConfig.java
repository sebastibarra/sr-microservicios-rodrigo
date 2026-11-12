package com.reservas.ms_reservas.config;

import com.reservas.security.JwtSupportConfiguration;
import com.reservas.security.JwtTokenProvider;
import com.reservas.security.SecurityProperties;
import com.reservas.security.StatelessSecuritySupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Import(JwtSupportConfiguration.class)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			SecurityProperties properties,
			JwtTokenProvider tokenProvider,
			AuthenticationEntryPoint authenticationEntryPoint,
			AccessDeniedHandler accessDeniedHandler
	) throws Exception {
		return StatelessSecuritySupport.build(
				http,
				properties,
				tokenProvider,
				authenticationEntryPoint,
				accessDeniedHandler
		);
	}
}
