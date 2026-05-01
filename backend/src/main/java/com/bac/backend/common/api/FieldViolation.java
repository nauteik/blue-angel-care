package com.bac.backend.common.api;

public record FieldViolation(
    String field,
    String message) {
}
