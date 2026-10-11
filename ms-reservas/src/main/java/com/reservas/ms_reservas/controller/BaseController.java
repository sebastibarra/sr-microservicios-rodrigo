package com.reservas.ms_reservas.controller;

import com.reservas.ms_reservas.config.PaginationProperties;
import com.reservas.ms_reservas.dto.PageResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public abstract class BaseController {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final PaginationProperties pagination;

    protected BaseController(PaginationProperties pagination) {
        this.pagination = pagination;
    }

    protected Pageable pageable(int page, int size) {
        int boundedSize = Math.min(size, pagination.maxSize());
        return PageRequest.of(page, boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    protected <T> PageResponseDto<T> toResponse(Page<T> page) {
        return PageResponseDto.from(page);
    }
}
