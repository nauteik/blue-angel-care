package com.bac.backend.common.api;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.bac.backend")
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (shouldSkip(body, selectedContentType, request)) {
      return body;
    }

    if (body instanceof Page<?> page) {
      return ApiResponse.of(page);
    }

    return ApiResponse.of(body);
  }

  private boolean shouldSkip(Object body, MediaType selectedContentType, ServerHttpRequest request) {
    String path = request.getURI().getPath();

    return body == null
        || body instanceof ApiResponse<?>
        || body instanceof ErrorResponse
        || body instanceof ProblemDetail
        || body instanceof String
        || body instanceof byte[]
        || body instanceof Resource
        || path.startsWith("/actuator")
        || !isJsonResponse(selectedContentType);
  }

  private boolean isJsonResponse(MediaType mediaType) {
    return mediaType == null
        || MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)
        || mediaType.getSubtype().endsWith("+json");
  }
}
