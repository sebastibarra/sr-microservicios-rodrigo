package com.reservas.ms_reservas.service;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.commons.entity.ConsultationType;
import com.reservas.commons.exception.BadRequestException;
import com.reservas.commons.exception.ConflictException;
import com.reservas.ms_reservas.dto.ConsultationTypeRequestDto;
import com.reservas.ms_reservas.mapper.ConsultationTypeMapper;
import com.reservas.ms_reservas.repository.ConsultationTypeRepository;
import com.reservas.ms_reservas.repository.spec.ConsultationTypeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConsultationTypeService extends BaseService<ConsultationType> {

    private final ConsultationTypeRepository repository;
    private final ConsultationTypeMapper mapper;

    public ConsultationTypeService(ConsultationTypeRepository repository, ConsultationTypeMapper mapper) {
        super(repository, "Consultation type");
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public CatalogDto create(ConsultationTypeRequestDto request) {
        log.info("Creating consultation type '{}'", request.name());
        if (repository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("DUPLICATE_CONSULTATION_TYPE", "A consultation type with that name already exists");
        }
        ConsultationType saved = repository.save(mapper.toEntity(request));
        log.info("Consultation type {} created", saved.getId());
        return mapper.toDto(saved);
    }

    @Transactional
    public CatalogDto update(UUID id, ConsultationTypeRequestDto request) {
        log.info("Updating consultation type {}", id);
        ConsultationType entity = findOrThrow(id);
        if (repository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("DUPLICATE_CONSULTATION_TYPE", "A consultation type with that name already exists");
        }
        mapper.updateEntity(entity, request);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public CatalogDto get(UUID id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CatalogDto> list(Pageable pageable) {
        log.debug("Listing consultation types page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CatalogDto> search(String name, Boolean active, Pageable pageable) {
        log.debug("Searching consultation types name='{}' active={}", name, active);
        return repository.findAll(ConsultationTypeSpecifications.byFilters(name, active), pageable).map(mapper::toDto);
    }

    @Transactional
    public CatalogDto deactivate(UUID id) {
        log.info("Deactivating consultation type {}", id);
        ConsultationType entity = findOrThrow(id);
        entity.setActive(false);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ConsultationType getActiveEntity(UUID id) {
        ConsultationType entity = findOrThrow(id);
        assertActive(entity);
        return entity;
    }

    @Transactional(readOnly = true)
    public Map<UUID, ConsultationType> getActiveEntities(Collection<UUID> ids) {
        Map<UUID, ConsultationType> found = repository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(ConsultationType::getId, Function.identity()));
        List<UUID> missing = new HashSet<>(ids).stream().filter(id -> !found.containsKey(id)).toList();
        if (!missing.isEmpty()) {
            throw new BadRequestException("UNKNOWN_CONSULTATION_TYPE", "Unknown consultation types: " + missing);
        }
        found.values().forEach(this::assertActive);
        return found;
    }

    private void assertActive(ConsultationType entity) {
        if (!entity.isActive()) {
            throw new BadRequestException("INACTIVE_CONSULTATION_TYPE",
                    "Consultation type '" + entity.getName() + "' is inactive");
        }
    }
}
