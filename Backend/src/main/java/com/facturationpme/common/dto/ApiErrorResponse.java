package com.facturationpme.common.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Forme d'erreur exposee a l'API, alignee sur l'interface {@code IApiError} du frontend Angular
 * (core/models/api-response.model.ts), consommee par {@code
 * core/interceptors/error.interceptor.ts}.
 */
public record ApiErrorResponse(
    int status,
    String code,
    String message,
    Map<String, java.util.List<String>> errors,
    Instant timestamp,
    String path) {

  public static ApiErrorResponse of(int status, String code, String message, String path) {
    return new ApiErrorResponse(status, code, message, null, Instant.now(), path);
  }

  public static ApiErrorResponse ofValidation(
      String message, Map<String, java.util.List<String>> errors, String path) {
    return new ApiErrorResponse(422, "VALIDATION_ERROR", message, errors, Instant.now(), path);
  }
}
