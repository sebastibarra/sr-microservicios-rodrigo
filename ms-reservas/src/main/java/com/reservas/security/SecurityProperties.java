package com.reservas.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

	private final Jwt jwt = new Jwt();
	private List<String> publicPaths = new ArrayList<>();
	private List<AccessRule> rules = new ArrayList<>();

	public Jwt getJwt() {
		return jwt;
	}

	public List<String> getPublicPaths() {
		return publicPaths;
	}

	public void setPublicPaths(List<String> publicPaths) {
		this.publicPaths = publicPaths;
	}

	public List<AccessRule> getRules() {
		return rules;
	}

	public void setRules(List<AccessRule> rules) {
		this.rules = rules;
	}

	public static class Jwt {
		private String secret;
		private Duration expiration = Duration.ofHours(1);
		private Duration clockSkew = Duration.ofSeconds(30);
		private String issuer = "reservas-ms-users";
		private String tokenType = "Bearer";

		public String getSecret() {
			return secret;
		}

		public void setSecret(String secret) {
			this.secret = secret;
		}

		public Duration getExpiration() {
			return expiration;
		}

		public void setExpiration(Duration expiration) {
			this.expiration = expiration;
		}

		public Duration getClockSkew() {
			return clockSkew;
		}

		public void setClockSkew(Duration clockSkew) {
			this.clockSkew = clockSkew;
		}

		public String getIssuer() {
			return issuer;
		}

		public void setIssuer(String issuer) {
			this.issuer = issuer;
		}

		public String getTokenType() {
			return tokenType;
		}

		public void setTokenType(String tokenType) {
			this.tokenType = tokenType;
		}
	}

	public static class AccessRule {
		private String path;
		private List<String> methods = new ArrayList<>();
		private List<String> roles = new ArrayList<>();
		private boolean permitAll;

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public List<String> getMethods() {
			return methods;
		}

		public void setMethods(List<String> methods) {
			this.methods = methods;
		}

		public List<String> getRoles() {
			return roles;
		}

		public void setRoles(List<String> roles) {
			this.roles = roles;
		}

		public boolean isPermitAll() {
			return permitAll;
		}

		public void setPermitAll(boolean permitAll) {
			this.permitAll = permitAll;
		}
	}
}
