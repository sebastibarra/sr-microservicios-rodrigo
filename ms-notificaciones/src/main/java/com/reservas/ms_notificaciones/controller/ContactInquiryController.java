package com.reservas.ms_notificaciones.controller;

import com.reservas.commons.dto.ContactInquiryCreateDto;
import com.reservas.commons.dto.ContactInquiryDto;
import com.reservas.ms_notificaciones.config.PaginationProperties;
import com.reservas.commons.dto.PageResponseDto;
import com.reservas.commons.web.PaginatedBaseController;
import com.reservas.ms_notificaciones.service.ContactInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${app.api.base-path:}/contact")
@Tag(name = "Contact", description = "Public contact form forwarded to the platform support")
public class ContactInquiryController extends PaginatedBaseController {

    private final ContactInquiryService service;

    public ContactInquiryController(ContactInquiryService service, PaginationProperties pagination) {
        super(pagination);
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a contact inquiry to support")
    public ContactInquiryDto create(@Valid @RequestBody ContactInquiryCreateDto request) {
        log.info("POST /contact from={}", request.getEmail());
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a contact inquiry by id")
    public ContactInquiryDto get(@PathVariable UUID id) {
        log.info("GET /contact/{}", id);
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List contact inquiries (paginated)")
    public PageResponseDto<ContactInquiryDto> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        log.info("GET /contact page={} size={}", page, size);
        return toResponse(service.list(pageable(page, size)));
    }
}
