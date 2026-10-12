package com.reservas.ms_reservas.config;

import com.reservas.commons.pagination.PaginationSupport;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.pagination")
public record PaginationProperties(
        @DefaultValue("10") int defaultSize,
        @DefaultValue("100") int maxSize
) implements PaginationSupport {

    @Override
    public int getDefaultSize() {
        return defaultSize;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }
}
