package com.reservas.ms_reservas.controller;

import com.reservas.commons.dto.ReservationCreateDto;
import com.reservas.commons.dto.ReservationDto;
import com.reservas.commons.enums.ReservationStatus;
import com.reservas.ms_reservas.config.PaginationProperties;
import com.reservas.ms_reservas.dto.PageResponseDto;
import com.reservas.ms_reservas.dto.ReservationFilter;
import com.reservas.ms_reservas.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("${app.api.base-path:}/reservations")
@Tag(name = "Reservations", description = "Reservations (header) with their consultation types (detail, many-to-many)")
public class ReservationController extends BaseController {

    private final ReservationService service;

    public ReservationController(ReservationService service, PaginationProperties pagination) {
        super(pagination);
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a reservation with one or more consultation types")
    @ApiResponse(responseCode = "409", description = "The time slot is already taken")
    public ReservationDto create(@Valid @RequestBody ReservationCreateDto request) {
        log.info("POST /reservations student={} timeSlot={}", request.getStudentId(), request.getTimeSlotId());
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a reservation (time slot and consultation types)")
    public ReservationDto update(@PathVariable UUID id, @Valid @RequestBody ReservationCreateDto request) {
        log.info("PUT /reservations/{}", id);
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a reservation by id")
    public ReservationDto get(@PathVariable UUID id) {
        log.info("GET /reservations/{}", id);
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List reservations (paginated)")
    public PageResponseDto<ReservationDto> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        log.info("GET /reservations page={} size={}", page, size);
        return toResponse(service.list(pageable(page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search reservations by student, professor, consultation type, status and date range (paginated)")
    public PageResponseDto<ReservationDto> search(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID professorId,
            @RequestParam(required = false) UUID consultationTypeId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        ReservationFilter filter = new ReservationFilter(studentId, professorId, consultationTypeId, status, dateFrom, dateTo);
        log.info("GET /reservations/search {} page={} size={}", filter, page, size);
        return toResponse(service.search(filter, pageable(page, size)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Logical delete: cancels the reservation and releases its time slot")
    public ReservationDto delete(@PathVariable UUID id) {
        log.info("DELETE /reservations/{}", id);
        return service.cancel(id);
    }

    @PostMapping("/{id}/cancellation")
    @Operation(summary = "Cancel a reservation (same effect as DELETE)")
    public ReservationDto cancel(@PathVariable UUID id) {
        log.info("POST /reservations/{}/cancellation", id);
        return service.cancel(id);
    }
}
