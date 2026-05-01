package com.bac.backend.common.api;

import org.springframework.data.domain.Page;

public record PageMetadata(
    int number,
    int size,
    long totalElements,
    int totalPages) {

  public static PageMetadata from(Page<?> page) {
    return new PageMetadata(
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }
}
