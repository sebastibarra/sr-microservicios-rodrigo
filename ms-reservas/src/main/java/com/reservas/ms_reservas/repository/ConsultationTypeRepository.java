package com.reservas.ms_reservas.repository;

import com.reservas.commons.entity.ConsultationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationTypeRepository
        extends JpaRepository<ConsultationType, UUID>, JpaSpecificationExecutor<ConsultationType> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    List<ConsultationType> findByIdIn(Collection<UUID> ids);
}
