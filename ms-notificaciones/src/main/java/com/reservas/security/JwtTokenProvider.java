package com.reservas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class JwtTokenProvider {

	private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
	private static final int MIN_SECRET_BYTES = 32;

	private final SecurityProperties.Jwt settings;
	private final SecretKey key;
	private final Clock clock;

	public JwtTokenProvider(SecurityProperties properties) {
		this(properties, Clock.systemUTC());
	}

	JwtTokenProvider(SecurityProperties properties, Clock clock) {
		this.settings = properties.getJwt();
		this.clock = clock;
		this.key = hmacKey(settings.getSecret());
		if (settings.getIssuer() == null || settings.getIssuer().isBlank()) {
			throw new IllegalStateException("app.security.jwt.issuer is required");
		}
		if (settings.getExpiration() == null || settings.getExpiration().isZero() || settings.getExpiration().isNegative()) {
			throw new IllegalStateException("app.security.jwt.expiration must be a positive duration");
		}
		if (settings.getClockSkew() != null && settings.getClockSkew().isNegative()) {
			throw new IllegalStateException("app.security.jwt.clock-skew cannot be negative");
		}
	}

	public String generate(UUID userId, String email, Collection<Role> roles) {
		Instant now = clock.instant();
		Instant expiresAt = now.plus(settings.getExpiration());
		List<String> roleNames = roles.stream().map(Role::name).distinct().sorted().toList();
		log.info("Issuing access token userId={} roles={}", userId, roleNames);
		return Jwts.builder()
				.subject(userId.toString())
				.issuer(settings.getIssuer())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				.claim("email", email)
				.claim("roles", roleNames)
				.signWith(key)
				.compact();
	}

	public JwtClaims parse(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(key)
				.requireIssuer(settings.getIssuer())
				.clockSkewSeconds(settings.getClockSkew() == null ? 0 : settings.getClockSkew().toSeconds())
				.clock(() -> Date.from(clock.instant()))
				.build()
				.parseSignedClaims(token)
				.getPayload();
		UUID userId = UUID.fromString(claims.getSubject());
		String email = claims.get("email", String.class);
		List<String> roles = readRoles(claims.get("roles"));
		if (email == null || email.isBlank() || roles.isEmpty()) {
			throw new IllegalArgumentException("Token is missing required claims");
		}
		return new JwtClaims(userId, email, List.copyOf(roles));
	}

	public String tokenType() {
		return settings.getTokenType();
	}

	private static SecretKey hmacKey(String secret) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("app.security.jwt.secret is required");
		}
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("app.security.jwt.secret must be at least 32 bytes");
		}
		return Keys.hmacShaKeyFor(bytes);
	}

	private static List<String> readRoles(Object raw) {
		if (!(raw instanceof List<?> values)) {
			return List.of();
		}
		return values.stream()
				.filter(String.class::isInstance)
				.map(String.class::cast)
				.filter(role -> !role.isBlank())
				.toList();
	}
}
