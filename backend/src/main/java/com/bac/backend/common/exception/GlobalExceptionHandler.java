package com.bac.backend.common.exception;

import com.bac.backend.common.api.ApiError;
import com.bac.backend.common.api.ErrorResponse;
import com.bac.backend.common.api.FieldViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException exception) {
    return ResponseEntity
        .status(exception.status())
        .body(error(exception.code(), exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
    List<FieldViolation> details = exception.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(this::toFieldViolation)
        .toList();

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(error(ApiErrorCode.VALIDATION_ERROR, "Invalid request.", details));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
    List<FieldViolation> details = exception.getConstraintViolations()
        .stream()
        .map(violation -> new FieldViolation(
            violation.getPropertyPath().toString(),
            violation.getMessage()))
        .toList();

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(error(ApiErrorCode.VALIDATION_ERROR, "Invalid request.", details));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable() {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(error(ApiErrorCode.BAD_REQUEST, "Malformed request body."));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException exception) {
    List<FieldViolation> details = List.of(new FieldViolation(
        exception.getParameterName(),
        "Required parameter is missing."));

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(error(ApiErrorCode.VALIDATION_ERROR, "Invalid request.", details));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
    List<FieldViolation> details = List.of(new FieldViolation(
        exception.getName(),
        "Invalid parameter value."));

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(error(ApiErrorCode.VALIDATION_ERROR, "Invalid request.", details));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception) {
    String message = exception.getReason() == null ? "Request failed." : exception.getReason();

    return ResponseEntity
        .status(exception.getStatusCode())
        .body(error(codeFor(exception.getStatusCode()), message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
    log.error("Unhandled exception", exception);

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(error(ApiErrorCode.INTERNAL_ERROR, "Unexpected server error."));
  }

  private FieldViolation toFieldViolation(FieldError fieldError) {
    String message = fieldError.getDefaultMessage() == null
        ? "Invalid value."
        : fieldError.getDefaultMessage();

    return new FieldViolation(fieldError.getField(), message);
  }

  private String codeFor(HttpStatusCode status) {
    if (status == HttpStatus.UNAUTHORIZED) {
      return ApiErrorCode.UNAUTHORIZED;
    }
    if (status == HttpStatus.FORBIDDEN) {
      return ApiErrorCode.FORBIDDEN;
    }
    if (status == HttpStatus.NOT_FOUND) {
      return ApiErrorCode.NOT_FOUND;
    }
    if (status == HttpStatus.CONFLICT) {
      return ApiErrorCode.CONFLICT;
    }
    if (status.is4xxClientError()) {
      return ApiErrorCode.BAD_REQUEST;
    }
    return ApiErrorCode.INTERNAL_ERROR;
  }

  private ErrorResponse error(String code, String message) {
    return new ErrorResponse(ApiError.of(code, message));
  }

  private ErrorResponse error(String code, String message, List<FieldViolation> details) {
    return new ErrorResponse(ApiError.of(code, message, details));
  }
}
