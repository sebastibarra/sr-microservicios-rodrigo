package com.reservas.ms_reservas.service;

import com.reservas.commons.entity.Setting;
import com.reservas.ms_reservas.repository.SettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingService extends BaseService<Setting> {

    private final SettingRepository repository;

    public SettingService(SettingRepository repository) {
        super(repository, "Setting");
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public int getInt(String key, int defaultValue) {
        return repository.findBySettingKey(key)
                .map(Setting::getValue)
                .map(value -> parse(key, value, defaultValue))
                .orElse(defaultValue);
    }

    private int parse(String key, String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Setting '{}' has a non numeric value '{}', using default {}", key, value, defaultValue);
            return defaultValue;
        }
    }
}
