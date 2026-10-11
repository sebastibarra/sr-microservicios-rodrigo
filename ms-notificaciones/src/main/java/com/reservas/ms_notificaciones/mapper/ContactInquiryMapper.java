package com.reservas.ms_notificaciones.mapper;

import com.reservas.commons.dto.ContactInquiryCreateDto;
import com.reservas.commons.dto.ContactInquiryDto;
import com.reservas.commons.entity.ContactInquiry;
import org.springframework.stereotype.Component;

@Component
public class ContactInquiryMapper {

    public ContactInquiryDto toDto(ContactInquiry entity) {
        return new ContactInquiryDto()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .message(entity.getMessage());
    }

    public ContactInquiry toEntity(ContactInquiryCreateDto dto) {
        ContactInquiry entity = new ContactInquiry();
        entity.setName(dto.getName().trim());
        entity.setEmail(dto.getEmail().trim());
        entity.setMessage(dto.getMessage().trim());
        return entity;
    }
}
