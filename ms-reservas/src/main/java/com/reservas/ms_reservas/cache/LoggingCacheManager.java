package com.reservas.ms_reservas.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LoggingCacheManager implements CacheManager {

	private final CacheManager delegate;
	private final ConcurrentMap<String, Cache> wrappedCaches = new ConcurrentHashMap<>();

	public LoggingCacheManager(CacheManager delegate) {
		this.delegate = delegate;
	}

	@Override
	public Cache getCache(String name) {
		Cache cache = delegate.getCache(name);
		return cache == null ? null : wrappedCaches.computeIfAbsent(name, ignored -> new LoggingCache(cache));
	}

	@Override
	public Collection<String> getCacheNames() {
		return delegate.getCacheNames();
	}

	private static final class LoggingCache implements Cache {

		private static final Logger log = LoggerFactory.getLogger(LoggingCache.class);
		private final Cache delegate;

		private LoggingCache(Cache delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public Object getNativeCache() {
			return delegate.getNativeCache();
		}

		@Override
		public ValueWrapper get(Object key) {
			ValueWrapper value = delegate.get(key);
			log.debug("Cache GET domain={} key={} hit={}", getName(), key, value != null);
			return value;
		}

		@Override
		public <T> T get(Object key, Class<T> type) {
			T value = delegate.get(key, type);
			log.debug("Cache GET domain={} key={} hit={}", getName(), key, value != null);
			return value;
		}

		@Override
		public <T> T get(Object key, Callable<T> valueLoader) {
			log.debug("Cache GET/LOAD domain={} key={}", getName(), key);
			return delegate.get(key, valueLoader);
		}

		@Override
		public void put(Object key, Object value) {
			delegate.put(key, value);
			log.debug("Cache SET domain={} key={}", getName(), key);
		}

		@Override
		public ValueWrapper putIfAbsent(Object key, Object value) {
			ValueWrapper existing = delegate.putIfAbsent(key, value);
			log.debug("Cache SET_IF_ABSENT domain={} key={} inserted={}", getName(), key, existing == null);
			return existing;
		}

		@Override
		public void evict(Object key) {
			delegate.evict(key);
			log.debug("Cache DELETE domain={} key={}", getName(), key);
		}

		@Override
		public boolean evictIfPresent(Object key) {
			boolean removed = delegate.evictIfPresent(key);
			log.debug("Cache DELETE_IF_PRESENT domain={} key={} removed={}", getName(), key, removed);
			return removed;
		}

		@Override
		public void clear() {
			delegate.clear();
			log.info("Cache CLEAR domain={}", getName());
		}

		@Override
		public boolean invalidate() {
			boolean invalidated = delegate.invalidate();
			log.info("Cache INVALIDATE domain={} invalidated={}", getName(), invalidated);
			return invalidated;
		}
	}
}
