package com.bac.backend.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
    String code,
    String message,
    List<FieldViolation> details) {

  public static ApiError of(String code, String message) {
    return new ApiError(code, message, List.of());
  }

  public static ApiError of(String code, String message, List<FieldViolation> details) {
    return new ApiError(code, message, details);
  }
}
