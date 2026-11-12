package com.reservas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider tokenProvider;
	private final List<RequestMatcher> publicPaths;

	public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, SecurityProperties properties) {
		this.tokenProvider = tokenProvider;
		List<RequestMatcher> matchers = new ArrayList<>();
		for (String path : properties.getPublicPaths()) {
			if (StringUtils.hasText(path)) {
				matchers.add(PathPatternRequestMatcher.pathPattern(path.trim()));
			}
		}
		this.publicPaths = List.copyOf(matchers);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return publicPaths.stream().anyMatch(matcher -> matcher.matches(request));
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = header.substring(BEARER_PREFIX.length()).trim();
		if (token.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}

		JwtClaims claims;
		try {
			claims = tokenProvider.parse(token);
		} catch (RuntimeException ex) {
			SecurityContextHolder.clearContext();
			log.warn("Rejected access token path={} reason={}", request.getRequestURI(), ex.getClass().getSimpleName());
			RestSecurityHandlers.writeUnauthorized(response, request, "Invalid or expired token");
			return;
		}

		AuthenticatedUser principal = new AuthenticatedUser(claims.userId(), claims.email(), claims.roles());
		List<SimpleGrantedAuthority> authorities = claims.roles().stream()
				.map(SimpleGrantedAuthority::new)
				.toList();
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(principal, null, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}
}
