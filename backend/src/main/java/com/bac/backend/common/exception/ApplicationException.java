package com.bac.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class ApplicationException extends RuntimeException {

  private final String code;
  private final HttpStatusCode status;

  public ApplicationException(String code, String message, HttpStatusCode status) {
    super(message);
    this.code = code;
    this.status = status;
  }

  public static ApplicationException notFound(String message) {
    return new ApplicationException(ApiErrorCode.NOT_FOUND, message, HttpStatus.NOT_FOUND);
  }

  public static ApplicationException conflict(String message) {
    return new ApplicationException(ApiErrorCode.CONFLICT, message, HttpStatus.CONFLICT);
  }

  public static ApplicationException badRequest(String message) {
    return new ApplicationException(ApiErrorCode.BAD_REQUEST, message, HttpStatus.BAD_REQUEST);
  }

  public String code() {
    return code;
  }

  public HttpStatusCode status() {
    return status;
  }
}
