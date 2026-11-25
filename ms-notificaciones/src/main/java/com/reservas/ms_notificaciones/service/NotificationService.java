package com.reservas.ms_notificaciones.service;

import com.reservas.commons.dto.NotificationDto;
import com.reservas.commons.entity.EmailTemplate;
import com.reservas.commons.entity.Notification;
import com.reservas.commons.entity.User;
import com.reservas.commons.enums.NotificationStatus;
import com.reservas.ms_notificaciones.dto.NotificationFilter;
import com.reservas.ms_notificaciones.dto.NotificationRequestDto;
import com.reservas.ms_notificaciones.mail.MailGateway;
import com.reservas.ms_notificaciones.mail.TemplateRenderer;
import com.reservas.ms_notificaciones.mapper.NotificationMapper;
import com.reservas.ms_notificaciones.repository.NotificationRepository;
import com.reservas.ms_notificaciones.repository.spec.NotificationSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Limit;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService extends BaseService<Notification> {

    private static final int MAX_ERROR_LENGTH = 500;
    private static final int DISPATCH_BATCH_SIZE = 50;

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final UserService userService;
    private final EmailTemplateService emailTemplateService;
    private final TemplateRenderer renderer;
    private final MailGateway mailGateway;
    private final Clock clock;

    public NotificationService(
            NotificationRepository repository,
            NotificationMapper mapper,
            UserService userService,
            EmailTemplateService emailTemplateService,
            TemplateRenderer renderer,
            MailGateway mailGateway,
            Clock clock
    ) {
        super(repository, "Notification");
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
        this.emailTemplateService = emailTemplateService;
        this.renderer = renderer;
        this.mailGateway = mailGateway;
        this.clock = clock;
    }

    /**
     * Encola una notificación en estado PENDING. No envía el correo: lo hace la tarea programada
     * ({@link NotificationDispatcher}) o {@link #dispatchPending()} desde el endpoint manual.
     */
    @Transactional
    @CacheEvict(cacheNames = "notifications", allEntries = true)
    public NotificationDto send(NotificationRequestDto request) {
        log.info("Queueing {} notification for user {}", request.type(), request.userId());
        User user = userService.getActiveUser(request.userId());
        emailTemplateService.getActiveByCode(request.type().name());   // valida que exista la plantilla
        Notification notification = mapper.toEntity(request, user);
        notification.setStatus(NotificationStatus.PENDING);
        return mapper.toDto(repository.save(notification));
    }

    /** Resultado de una pasada de envío. */
    public record DispatchResult(int processed, int sent, int failed, long remaining) {
    }

    /**
     * Envía un lote de notificaciones pendientes. Las filas se bloquean con SKIP LOCKED, así que
     * dos ejecuciones concurrentes (cron y botón, o dos instancias) nunca toman la misma.
     */
    @Transactional
    @CacheEvict(cacheNames = "notifications", allEntries = true)
    public DispatchResult dispatchPending() {
        List<Notification> batch = repository.findByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING, Limit.of(DISPATCH_BATCH_SIZE));
        int sent = 0;
        for (Notification notification : batch) {
            if (deliver(notification)) {
                sent++;
            }
        }
        repository.saveAll(batch);
        long remaining = repository.countByStatus(NotificationStatus.PENDING) - batch.size();
        return new DispatchResult(batch.size(), sent, batch.size() - sent, Math.max(remaining, 0));
    }

    private boolean deliver(Notification notification) {
        try {
            User user = notification.getUser();
            EmailTemplate template = emailTemplateService.getActiveByCode(notification.getType().name());
            Map<String, Object> values = new HashMap<>(notification.getPayload() == null ? Map.of() : notification.getPayload());
            values.putIfAbsent("recipientName", user.getFirstName() + " " + user.getLastName());
            mailGateway.send(
                    user.getEmail(),
                    renderer.renderSubject(template.getSubject(), values),
                    renderer.renderHtml(template.getHtmlBody(), values)
            );
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now(clock));
            notification.setErrorMessage(null);
            log.info("Notification {} ({}) sent to {}", notification.getId(), notification.getType(), user.getEmail());
            return true;
        } catch (RuntimeException e) {
            notification.setStatus(NotificationStatus.ERROR);
            notification.setErrorMessage(truncate(e.getMessage()));
            log.error("Notification {} could not be delivered", notification.getId(), e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "notifications", key = "#id")
    public NotificationDto get(UUID id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> list(Pageable pageable) {
        log.debug("Listing notifications page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> search(NotificationFilter filter, Pageable pageable) {
        log.debug("Searching notifications with {}", filter);
        return repository.findAll(NotificationSpecifications.byFilter(filter), pageable).map(mapper::toDto);
    }

    private static String truncate(String message) {
        if (message == null) {
            return "Unknown delivery error";
        }
        return message.length() > MAX_ERROR_LENGTH ? message.substring(0, MAX_ERROR_LENGTH) : message;
    }
}
