package com.reservas.ms_notificaciones.service;

import com.reservas.commons.entity.base.BaseEntity;
import com.reservas.commons.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public abstract class BaseService<E extends BaseEntity> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final JpaRepository<E, UUID> repository;
    private final String entityName;

    protected BaseService(JpaRepository<E, UUID> repository, String entityName) {
        this.repository = repository;
        this.entityName = entityName;
    }

    protected E findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> {
            log.warn("{} {} not found", entityName, id);
            return new ResourceNotFoundException(entityName + " not found: " + id);
        });
    }
}
