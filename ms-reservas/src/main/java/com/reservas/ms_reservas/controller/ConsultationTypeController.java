package com.reservas.ms_reservas.controller;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.ms_reservas.config.PaginationProperties;
import com.reservas.ms_reservas.dto.ConsultationTypeRequestDto;
import com.reservas.commons.dto.PageResponseDto;
import com.reservas.commons.web.PaginatedBaseController;
import com.reservas.ms_reservas.service.ConsultationTypeService;
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
@RequestMapping("${app.api.base-path:}/consultation-types")
@Tag(name = "Consultation types", description = "Catalog of consultation types (simple entity)")
public class ConsultationTypeController extends PaginatedBaseController {

    private final ConsultationTypeService service;

    public ConsultationTypeController(ConsultationTypeService service, PaginationProperties pagination) {
        super(pagination);
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a consultation type")
    public CatalogDto create(@Valid @RequestBody ConsultationTypeRequestDto request) {
        log.info("POST /consultation-types name={}", request.name());
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a consultation type")
    public CatalogDto update(@PathVariable UUID id, @Valid @RequestBody ConsultationTypeRequestDto request) {
        log.info("PUT /consultation-types/{}", id);
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a consultation type by id")
    public CatalogDto get(@PathVariable UUID id) {
        log.info("GET /consultation-types/{}", id);
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List consultation types (paginated)")
    public PageResponseDto<CatalogDto> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        log.info("GET /consultation-types page={} size={}", page, size);
        return toResponse(service.list(pageable(page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search consultation types by name and status (paginated)")
    public PageResponseDto<CatalogDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        log.info("GET /consultation-types/search name={} active={} page={} size={}", name, active, page, size);
        return toResponse(service.search(name, active, pageable(page, size)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Logical delete: marks the consultation type as inactive")
    public CatalogDto delete(@PathVariable UUID id) {
        log.info("DELETE /consultation-types/{}", id);
        return service.deactivate(id);
    }
}
