package com.reservas.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

public final class RestSecurityHandlers {

	private static final Logger log = LoggerFactory.getLogger(RestSecurityHandlers.class);

	private RestSecurityHandlers() {
	}

	public static AuthenticationEntryPoint authenticationEntryPoint() {
		return (request, response, authException) -> {
			log.warn("Unauthorized path={} reason={}", request.getRequestURI(), authException.getClass().getSimpleName());
			write(response, request, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required");
		};
	}

	public static AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			log.warn("Forbidden path={} reason={}", request.getRequestURI(), accessDeniedException.getClass().getSimpleName());
			write(response, request, HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to access this resource");
		};
	}

	public static void writeUnauthorized(HttpServletResponse response, HttpServletRequest request, String message) throws IOException {
		write(response, request, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
	}

	private static void write(
			HttpServletResponse response,
			HttpServletRequest request,
			HttpStatus status,
			String code,
			String message
	) throws IOException {
		response.setStatus(status.value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		String body = "{"
				+ "\"status\":" + status.value()
				+ ",\"code\":\"" + code + "\""
				+ ",\"message\":\"" + escape(message) + "\""
				+ ",\"timestamp\":\"" + OffsetDateTime.now() + "\""
				+ ",\"path\":\"" + escape(request.getRequestURI()) + "\""
				+ "}";
		response.getWriter().write(body);
	}

	private static String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
