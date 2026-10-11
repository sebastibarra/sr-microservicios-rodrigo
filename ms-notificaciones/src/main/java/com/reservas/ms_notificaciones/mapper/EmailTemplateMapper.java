package com.reservas.ms_notificaciones.mapper;

import com.reservas.commons.dto.EmailTemplateDto;
import com.reservas.commons.entity.EmailTemplate;
import com.reservas.ms_notificaciones.dto.EmailTemplateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateMapper {

    public EmailTemplateDto toDto(EmailTemplate entity) {
        return new EmailTemplateDto()
                .id(entity.getId())
                .code(entity.getCode())
                .subject(entity.getSubject())
                .htmlBody(entity.getHtmlBody())
                .active(entity.isActive());
    }

    public EmailTemplate toEntity(EmailTemplateRequestDto request) {
        EmailTemplate entity = new EmailTemplate();
        updateEntity(entity, request);
        return entity;
    }

    public void updateEntity(EmailTemplate entity, EmailTemplateRequestDto request) {
        entity.setCode(request.code().trim());
        entity.setSubject(request.subject().trim());
        entity.setHtmlBody(request.htmlBody());
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }
}
