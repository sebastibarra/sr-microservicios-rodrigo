package com.reservas.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class JwtSupportConfiguration {

	@Bean
	public JwtTokenProvider jwtTokenProvider(SecurityProperties properties) {
		return new JwtTokenProvider(properties);
	}

	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		return RestSecurityHandlers.authenticationEntryPoint();
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return RestSecurityHandlers.accessDeniedHandler();
	}

	@Bean
	public UserDetailsService rejectingUserDetailsService() {
		return username -> {
			throw new UsernameNotFoundException("Invalid email or password");
		};
	}
}
