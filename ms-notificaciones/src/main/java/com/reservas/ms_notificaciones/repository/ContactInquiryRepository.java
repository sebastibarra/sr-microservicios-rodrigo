package com.reservas.ms_notificaciones.repository;

import com.reservas.commons.entity.ContactInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactInquiryRepository extends JpaRepository<ContactInquiry, UUID> {
}
