package com.reservas.ms_notificaciones.controller;

import com.reservas.commons.dto.EmailTemplateDto;
import com.reservas.ms_notificaciones.config.PaginationProperties;
import com.reservas.ms_notificaciones.dto.EmailTemplateRequestDto;
import com.reservas.commons.dto.PageResponseDto;
import com.reservas.commons.web.PaginatedBaseController;
import com.reservas.ms_notificaciones.service.EmailTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${app.api.base-path:}/email-templates")
@Tag(name = "Email templates", description = "Email templates configurable by the administrator (simple entity)")
public class EmailTemplateController extends PaginatedBaseController {

    private final EmailTemplateService service;

    public EmailTemplateController(EmailTemplateService service, PaginationProperties pagination) {
        super(pagination);
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an email template")
    public EmailTemplateDto create(@Valid @RequestBody EmailTemplateRequestDto request) {
        log.info("POST /email-templates code={}", request.code());
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an email template")
    public EmailTemplateDto update(@PathVariable UUID id, @Valid @RequestBody EmailTemplateRequestDto request) {
        log.info("PUT /email-templates/{}", id);
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an email template by id")
    public EmailTemplateDto get(@PathVariable UUID id) {
        log.info("GET /email-templates/{}", id);
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List email templates (paginated)")
    public PageResponseDto<EmailTemplateDto> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        log.info("GET /email-templates page={} size={}", page, size);
        return toResponse(service.list(pageable(page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search email templates by code/subject and status (paginated)")
    public PageResponseDto<EmailTemplateDto> search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        log.info("GET /email-templates/search text={} active={} page={} size={}", text, active, page, size);
        return toResponse(service.search(text, active, pageable(page, size)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Logical delete: marks the email template as inactive")
    public EmailTemplateDto delete(@PathVariable UUID id) {
        log.info("DELETE /email-templates/{}", id);
        return service.deactivate(id);
    }
}
