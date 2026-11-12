package com.reservas.ms_notificaciones.config;

import com.reservas.ms_notificaciones.cache.LoggingCacheManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfiguration {

	@Bean
	@Profile("!test")
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory, CacheProperties properties) {
		if (properties.getKeyPrefix() == null || properties.getKeyPrefix().isBlank()) {
			throw new IllegalStateException("app.cache.key-prefix is required");
		}
		Duration defaultTtl = requirePositiveTtl(properties.getDefaultTtl(), "app.cache.default-ttl");
		RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(defaultTtl)
				.disableCachingNullValues()
				.computePrefixWith(name -> properties.getKeyPrefix() + ":" + name + ":")
				.serializeValuesWith(RedisSerializationContext.SerializationPair
						.fromSerializer(new GenericJackson2JsonRedisSerializer()));
		Map<String, RedisCacheConfiguration> domains = new LinkedHashMap<>();
		properties.getDomains().forEach((name, domain) -> {
			if (domain == null) {
				throw new IllegalStateException("Cache domain configuration is required for " + name);
			}
			Duration ttl = domain.isPersistent()
					? Duration.ZERO
					: requirePositiveTtl(domain.getTtl() == null ? defaultTtl : domain.getTtl(),
							"app.cache.domains." + name + ".ttl");
			domains.put(name, defaults.entryTtl(ttl));
		});
		RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaults)
				.withInitialCacheConfigurations(domains)
				.build();
		return new LoggingCacheManager(redisCacheManager);
	}

	private static Duration requirePositiveTtl(Duration ttl, String property) {
		if (ttl == null || ttl.isZero() || ttl.isNegative()) {
			throw new IllegalStateException(property + " must be a positive duration unless the domain is persistent");
		}
		return ttl;
	}
}
