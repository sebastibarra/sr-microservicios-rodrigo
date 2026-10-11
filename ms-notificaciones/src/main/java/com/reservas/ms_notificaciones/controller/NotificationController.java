package com.reservas.ms_notificaciones.controller;

import com.reservas.commons.dto.NotificationDto;
import com.reservas.commons.enums.NotificationStatus;
import com.reservas.commons.enums.NotificationType;
import com.reservas.ms_notificaciones.config.PaginationProperties;
import com.reservas.ms_notificaciones.dto.NotificationFilter;
import com.reservas.ms_notificaciones.dto.NotificationRequestDto;
import com.reservas.ms_notificaciones.dto.PageResponseDto;
import com.reservas.ms_notificaciones.service.NotificationService;
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
@RequestMapping("${app.api.base-path:}/notifications")
@Tag(name = "Notifications", description = "Sending and history of notifications. Records are an audit log and are never deleted")
public class NotificationController extends BaseController {

    private final NotificationService service;

    public NotificationController(NotificationService service, PaginationProperties pagination) {
        super(pagination);
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a notification to a user using the template of its type")
    public NotificationDto send(@Valid @RequestBody NotificationRequestDto request) {
        log.info("POST /notifications user={} type={}", request.userId(), request.type());
        return service.send(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a notification by id")
    public NotificationDto get(@PathVariable UUID id) {
        log.info("GET /notifications/{}", id);
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List notifications (paginated)")
    public PageResponseDto<NotificationDto> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        log.info("GET /notifications page={} size={}", page, size);
        return toResponse(service.list(pageable(page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search notifications by user, type and status (paginated)")
    public PageResponseDto<NotificationDto> search(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "${app.pagination.default-size:10}") @Min(1) int size
    ) {
        NotificationFilter filter = new NotificationFilter(userId, type, status);
        log.info("GET /notifications/search {} page={} size={}", filter, page, size);
        return toResponse(service.search(filter, pageable(page, size)));
    }
}
