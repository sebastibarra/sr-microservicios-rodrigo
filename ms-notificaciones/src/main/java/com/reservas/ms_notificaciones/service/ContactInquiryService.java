package com.reservas.ms_notificaciones.service;

import com.reservas.commons.dto.ContactInquiryCreateDto;
import com.reservas.commons.dto.ContactInquiryDto;
import com.reservas.commons.entity.ContactInquiry;
import com.reservas.commons.exception.BadRequestException;
import com.reservas.ms_notificaciones.config.MailSettings;
import com.reservas.ms_notificaciones.mail.MailGateway;
import com.reservas.ms_notificaciones.mapper.ContactInquiryMapper;
import com.reservas.ms_notificaciones.repository.ContactInquiryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ContactInquiryService extends BaseService<ContactInquiry> {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@<>\"]+@[^\\s@<>\"]+\\.[^\\s@<>\"]+$");

    private final ContactInquiryRepository repository;
    private final ContactInquiryMapper mapper;
    private final MailGateway mailGateway;
    private final MailSettings mailSettings;

    public ContactInquiryService(
            ContactInquiryRepository repository,
            ContactInquiryMapper mapper,
            MailGateway mailGateway,
            MailSettings mailSettings
    ) {
        super(repository, "Contact inquiry");
        this.repository = repository;
        this.mapper = mapper;
        this.mailGateway = mailGateway;
        this.mailSettings = mailSettings;
    }

    public ContactInquiryDto create(ContactInquiryCreateDto request) {
        log.info("Registering contact inquiry from {}", request.getEmail());
        validate(request);
        ContactInquiry saved = repository.save(mapper.toEntity(request));
        forwardToSupport(saved);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public ContactInquiryDto get(UUID id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ContactInquiryDto> list(Pageable pageable) {
        log.debug("Listing contact inquiries page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(mapper::toDto);
    }

    private void validate(ContactInquiryCreateDto request) {
        if (!EMAIL.matcher(request.getEmail().trim()).matches() || request.getEmail().trim().length() > 255) {
            throw new BadRequestException("email must be a valid address");
        }
        if (request.getName().isBlank() || request.getName().trim().length() > 150) {
            throw new BadRequestException("name must have between 1 and 150 characters");
        }
        if (request.getMessage().isBlank() || request.getMessage().trim().length() > 2000) {
            throw new BadRequestException("message must have between 1 and 2000 characters");
        }
    }

    private void forwardToSupport(ContactInquiry inquiry) {
        String subject = "Contact form: " + inquiry.getName().replaceAll("[\\r\\n]+", " ");
        String body = "<p><strong>" + HtmlUtils.htmlEscape(inquiry.getName()) + "</strong> &lt;"
                + HtmlUtils.htmlEscape(inquiry.getEmail()) + "&gt;</p><p>"
                + HtmlUtils.htmlEscape(inquiry.getMessage()) + "</p>";
        try {
            mailGateway.send(mailSettings.supportAddress(), subject, body);
        } catch (RuntimeException e) {
            log.error("Contact inquiry {} was stored but could not be forwarded to support", inquiry.getId(), e);
        }
    }
}
