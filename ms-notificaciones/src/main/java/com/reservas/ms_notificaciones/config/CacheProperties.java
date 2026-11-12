package com.reservas.ms_notificaciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

	private String keyPrefix;
	private Duration defaultTtl;
	private Map<String, Domain> domains = new LinkedHashMap<>();

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public Duration getDefaultTtl() {
		return defaultTtl;
	}

	public void setDefaultTtl(Duration defaultTtl) {
		this.defaultTtl = defaultTtl;
	}

	public Map<String, Domain> getDomains() {
		return domains;
	}

	public void setDomains(Map<String, Domain> domains) {
		this.domains = domains;
	}

	public static class Domain {
		private Duration ttl;
		private boolean persistent;

		public Duration getTtl() {
			return ttl;
		}

		public void setTtl(Duration ttl) {
			this.ttl = ttl;
		}

		public boolean isPersistent() {
			return persistent;
		}

		public void setPersistent(boolean persistent) {
			this.persistent = persistent;
		}
	}
}
