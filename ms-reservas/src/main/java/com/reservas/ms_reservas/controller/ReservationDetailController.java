package com.reservas.ms_reservas.controller;

import com.reservas.commons.dto.ReservationDetailCreateDto;
import com.reservas.commons.dto.ReservationDetailDto;
import com.reservas.ms_reservas.config.PaginationProperties;
import com.reservas.commons.dto.PageResponseDto;
import com.reservas.commons.web.PaginatedBaseController;
import com.reservas.ms_reservas.service.ReservationDetailService;
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
@RequestMapping("${app.api.base-path:}/reservations/{reservationId}/details")
@Tag(name = "Reservation details", description = "Reservation - consultation type association (many-to-many bridge table)")
public class ReservationDetailController extends PaginatedBaseController {

    private final ReservationDetailService service;

    public ReservationDetailController(ReservationDetailService service, PaginationProperties pagination) {
        super(pagination);
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Associate a consultation type to the reservation")
    public ReservationDetailDto add(
            @PathVariable UUID reservationId,
            @Valid @RequestBody ReservationDetailCreateDto request
    ) {
        log.info("POST /reservations/{}/details type={}", reservationId, request.getConsultationTypeId());
        return service.add(reservationId, request);
    }

    @PutMapping("/{detailId}")
    @Operation(summary = "Update an association (consultation type or comment)")
    public ReservationDetailDto update(
            @PathVariable UUID reservationId,
            @PathVariable UUID detailId,
            @Valid @RequestBody ReservationDetailCreateDto request
    ) {
        log.info("PUT /reservations/{}/details/{}", reservationId, detailId);
        return service.update(reservationId, detailId, request);
    }

    @GetMapping("/{detailId}")
    @Operation(summary = "Get an association by id")
    public ReservationDetailDto get(@PathVariable UUID reservationId, @PathVariable UUID detailId) {
        log.info("GET /reservations/{}/details/{}", reservationId, detailId);
        return service.get(reservationId, detailId);
    }

    @GetMapping
    @Operation(summary = "List the consultation types of a reservation (paginated)")
    public PageResponseDto<ReservationDetailDto> list(
            @PathVariable UUID reservationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        log.info("GET /reservations/{}/details page={} size={}", reservationId, page, size);
        return toResponse(service.list(reservationId, pageable(page, size)));
    }

    @DeleteMapping("/{detailId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an association (a reservation keeps at least one consultation type)")
    public void remove(@PathVariable UUID reservationId, @PathVariable UUID detailId) {
        log.info("DELETE /reservations/{}/details/{}", reservationId, detailId);
        service.remove(reservationId, detailId);
    }
}
