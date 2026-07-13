package com.facturationpme.common.exception;

import com.facturationpme.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Point unique de traduction des exceptions en reponses JSON, alignees sur le format {@code
 * IApiError} attendu par le frontend (core/interceptors/error.interceptor.ts) : 401 -> tentative de
 * refresh puis retry, 403 -> redirection /errors/403, 404 -> /errors/404, 500+ -> /errors/500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, List<String>> errors = new LinkedHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            fieldError ->
                errors
                    .computeIfAbsent(fieldError.getField(), key -> new java.util.ArrayList<>())
                    .add(fieldError.getDefaultMessage()));
    ApiErrorResponse body =
        ApiErrorResponse.ofValidation(
            "Des erreurs de validation sont survenues.", errors, request.getRequestURI());
    return ResponseEntity.unprocessableEntity().body(body);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()));
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiErrorResponse> handleDuplicate(
      DuplicateResourceException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "DUPLICATE_RESOURCE",
                ex.getMessage(),
                request.getRequestURI()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "DATA_INTEGRITY_VIOLATION",
                "Cette operation viole une contrainte d'integrite des donnees.",
                request.getRequestURI()));
  }

  @ExceptionHandler({ForbiddenActionException.class, AccessDeniedException.class})
  public ResponseEntity<ApiErrorResponse> handleForbidden(
      RuntimeException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            ApiErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                "Vous n'avez pas les permissions necessaires pour cette action.",
                request.getRequestURI()));
  }

  @ExceptionHandler(InvalidStateException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidState(
      InvalidStateException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ApiErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "INVALID_STATE",
                ex.getMessage(),
                request.getRequestURI()));
  }

  @ExceptionHandler(PaymentGatewayUnavailableException.class)
  public ResponseEntity<ApiErrorResponse> handlePaymentGatewayUnavailable(
      PaymentGatewayUnavailableException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(
            ApiErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "PAYMENT_GATEWAY_UNAVAILABLE",
                ex.getMessage(),
                request.getRequestURI()));
  }

  @ExceptionHandler(InvalidWebhookSignatureException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidWebhookSignature(
      InvalidWebhookSignatureException ex, HttpServletRequest request) {
    LOG.warn("Signature de webhook invalide sur {} : {}", request.getRequestURI(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_WEBHOOK_SIGNATURE",
                "Signature de webhook invalide.",
                request.getRequestURI()));
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidToken(
      InvalidTokenException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_TOKEN",
                ex.getMessage(),
                request.getRequestURI()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiErrorResponse> handleBadCredentials(
      BadCredentialsException ex, HttpServletRequest request) {
    // Message porte par l'appelant (AuthenticationManager pour /auth/login, RefreshTokenService
    // pour /auth/refresh) : le contexte differe, un message generique serait trompeur.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                request.getRequestURI()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(
      Exception ex, HttpServletRequest request) {
    LOG.error("Erreur interne non geree sur {}", request.getRequestURI(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "Une erreur interne est survenue. Nos equipes ont ete notifiees.",
                request.getRequestURI()));
  }
}
