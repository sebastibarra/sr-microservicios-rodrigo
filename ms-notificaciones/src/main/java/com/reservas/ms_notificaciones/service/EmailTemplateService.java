package com.reservas.ms_notificaciones.service;

import com.reservas.commons.dto.EmailTemplateDto;
import com.reservas.commons.entity.EmailTemplate;
import com.reservas.commons.exception.ConflictException;
import com.reservas.commons.exception.InvalidOperationException;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.ms_notificaciones.dto.EmailTemplateRequestDto;
import com.reservas.ms_notificaciones.mapper.EmailTemplateMapper;
import com.reservas.ms_notificaciones.repository.EmailTemplateRepository;
import com.reservas.ms_notificaciones.repository.spec.EmailTemplateSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EmailTemplateService extends BaseService<EmailTemplate> {

    private final EmailTemplateRepository repository;
    private final EmailTemplateMapper mapper;

    public EmailTemplateService(EmailTemplateRepository repository, EmailTemplateMapper mapper) {
        super(repository, "Email template");
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public EmailTemplateDto create(EmailTemplateRequestDto request) {
        log.info("Creating email template '{}'", request.code());
        if (repository.existsByCode(request.code().trim())) {
            throw new ConflictException("DUPLICATE_EMAIL_TEMPLATE", "An email template with that code already exists");
        }
        EmailTemplate saved = repository.save(mapper.toEntity(request));
        log.info("Email template {} created", saved.getId());
        return mapper.toDto(saved);
    }

    @Transactional
    public EmailTemplateDto update(UUID id, EmailTemplateRequestDto request) {
        log.info("Updating email template {}", id);
        EmailTemplate entity = findOrThrow(id);
        if (repository.existsByCodeAndIdNot(request.code().trim(), id)) {
            throw new ConflictException("DUPLICATE_EMAIL_TEMPLATE", "An email template with that code already exists");
        }
        mapper.updateEntity(entity, request);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public EmailTemplateDto get(UUID id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<EmailTemplateDto> list(Pageable pageable) {
        log.debug("Listing email templates page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<EmailTemplateDto> search(String text, Boolean active, Pageable pageable) {
        log.debug("Searching email templates text='{}' active={}", text, active);
        return repository.findAll(EmailTemplateSpecifications.byFilters(text, active), pageable).map(mapper::toDto);
    }

    @Transactional
    public EmailTemplateDto deactivate(UUID id) {
        log.info("Deactivating email template {}", id);
        EmailTemplate entity = findOrThrow(id);
        entity.setActive(false);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public EmailTemplate getActiveByCode(String code) {
        EmailTemplate template = repository.findByCode(code).orElseThrow(() -> {
            log.warn("Email template with code {} not found", code);
            return new ResourceNotFoundException("Email template not found: " + code);
        });
        if (!template.isActive()) {
            throw new InvalidOperationException("Email template " + code + " is inactive");
        }
        return template;
    }
}
