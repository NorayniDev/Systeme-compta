package com.facturationpme.payments.web;

import com.facturationpme.common.dto.PageResponse;
import com.facturationpme.common.util.PageableFactory;
import com.facturationpme.payments.dto.CheckoutRequestDto;
import com.facturationpme.payments.dto.CheckoutResponseDto;
import com.facturationpme.payments.dto.PaymentCreateDto;
import com.facturationpme.payments.dto.PaymentResponse;
import com.facturationpme.payments.gateway.PaymentGatewayName;
import com.facturationpme.payments.service.PaymentService;
import com.facturationpme.pdf.service.PaymentReceiptPdfService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * N'expose ni PUT ni DELETE : un paiement enregistre est immuable, seule une ecriture inverse via
 * {@code /refund} permet de le corriger (registre financier, pas un CRUD generique).
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentReceiptPdfService paymentReceiptPdfService;

  @GetMapping
  @PreAuthorize("hasAuthority('payment:read')")
  public PageResponse<PaymentResponse> search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String direction,
      @RequestParam(required = false) String query) {
    Pageable pageable = PageableFactory.of(page, size, sort, direction);
    return PageResponse.of(paymentService.search(query, pageable));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('payment:read')")
  public PaymentResponse findById(@PathVariable UUID id) {
    return paymentService.findById(id);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('payment:create')")
  public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentCreateDto dto) {
    return ResponseEntity.status(201).body(paymentService.create(dto));
  }

  @PostMapping("/{id}/refund")
  @PreAuthorize("hasAuthority('payment:refund')")
  public PaymentResponse refund(@PathVariable UUID id) {
    return paymentService.refund(id);
  }

  @GetMapping(value = "/{id}/receipt", produces = MediaType.APPLICATION_PDF_VALUE)
  @PreAuthorize("hasAuthority('payment:read')")
  public ResponseEntity<byte[]> receipt(@PathVariable UUID id) {
    var pdf = paymentReceiptPdfService.generate(id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.fileName() + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf.content());
  }

  @PostMapping("/checkout")
  @PreAuthorize("hasAuthority('payment:create')")
  public CheckoutResponseDto checkout(@Valid @RequestBody CheckoutRequestDto dto) {
    return paymentService.initiateCheckout(dto);
  }

  /**
   * Public (voir SecurityConfig) : appele par le serveur du fournisseur de paiement, jamais par le
   * frontend. L'authenticite est garantie par la verification de signature dans {@code
   * PaymentGatewayProvider.parseWebhook}, pas par un token JWT.
   */
  @PostMapping("/webhooks/{provider}")
  public ResponseEntity<Void> webhook(
      @PathVariable PaymentGatewayName provider,
      @RequestBody String rawBody,
      @RequestHeader Map<String, String> headers) {
    paymentService.handleWebhook(provider, rawBody, headers);
    return ResponseEntity.noContent().build();
  }
}
