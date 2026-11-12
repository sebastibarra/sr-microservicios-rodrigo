package com.reservas.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.util.StringUtils;

import java.util.List;

public final class UrlAuthorizationCustomizer {

	private UrlAuthorizationCustomizer() {
	}

	public static void apply(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
			SecurityProperties properties
	) {
		for (String path : properties.getPublicPaths()) {
			if (StringUtils.hasText(path)) {
				auth.requestMatchers(PathPatternRequestMatcher.pathPattern(path.trim())).permitAll();
			}
		}
		for (SecurityProperties.AccessRule rule : properties.getRules()) {
			if (!StringUtils.hasText(rule.getPath())) {
				throw new IllegalStateException("app.security.rules path is required");
			}
			if (!rule.isPermitAll() && normalize(rule.getRoles()).isEmpty()) {
				throw new IllegalStateException("app.security.rules roles are required for path " + rule.getPath());
			}
			List<String> roles = normalize(rule.getRoles());
			List<String> methods = normalize(rule.getMethods());
			if (methods.isEmpty()) {
				authorize(auth, rule, roles, null);
				continue;
			}
			for (String method : methods) {
				authorize(auth, rule, roles, HttpMethod.valueOf(method));
			}
		}
		auth.anyRequest().authenticated();
	}

	private static void authorize(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
			SecurityProperties.AccessRule rule,
			List<String> roles,
			HttpMethod method
	) {
		var matcher = method == null
				? PathPatternRequestMatcher.pathPattern(rule.getPath().trim())
				: PathPatternRequestMatcher.pathPattern(method, rule.getPath().trim());
		var request = auth.requestMatchers(matcher);
		if (rule.isPermitAll()) {
			request.permitAll();
		} else {
			request.hasAnyAuthority(roles.toArray(String[]::new));
		}
	}

	private static List<String> normalize(List<String> values) {
		if (values == null) {
			return List.of();
		}
		return values.stream()
				.filter(StringUtils::hasText)
				.map(String::trim)
				.map(String::toUpperCase)
				.toList();
	}
}
