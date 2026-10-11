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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService extends BaseService<Notification> {

    private static final int MAX_ERROR_LENGTH = 500;

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

    public NotificationDto send(NotificationRequestDto request) {
        log.info("Sending {} notification to user {}", request.type(), request.userId());
        User user = userService.getActiveUser(request.userId());
        EmailTemplate template = emailTemplateService.getActiveByCode(request.type().name());

        Notification notification = repository.save(mapper.toEntity(request, user));
        try {
            mailGateway.send(
                    user.getEmail(),
                    renderer.renderSubject(template.getSubject(), notification.getPayload()),
                    renderer.renderHtml(template.getHtmlBody(), notification.getPayload())
            );
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now(clock));
            log.info("Notification {} sent", notification.getId());
        } catch (RuntimeException e) {
            notification.setStatus(NotificationStatus.ERROR);
            notification.setErrorMessage(truncate(e.getMessage()));
            log.error("Notification {} could not be delivered", notification.getId(), e);
        }
        repository.save(notification);
        return mapper.toDto(notification);
    }

    @Transactional(readOnly = true)
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
