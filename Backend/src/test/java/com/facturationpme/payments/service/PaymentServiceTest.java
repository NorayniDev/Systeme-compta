package com.facturationpme.payments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturationpme.common.exception.InvalidStateException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.numbering.DocumentNumberGenerator;
import com.facturationpme.common.numbering.DocumentType;
import com.facturationpme.invoices.domain.Invoice;
import com.facturationpme.invoices.domain.InvoiceStatus;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.payments.domain.Payment;
import com.facturationpme.payments.domain.PaymentMethod;
import com.facturationpme.payments.domain.PaymentStatus;
import com.facturationpme.payments.dto.CheckoutRequestDto;
import com.facturationpme.payments.dto.CheckoutResponseDto;
import com.facturationpme.payments.dto.PaymentCreateDto;
import com.facturationpme.payments.dto.PaymentResponse;
import com.facturationpme.payments.event.PaymentReceivedEvent;
import com.facturationpme.payments.event.PaymentRefundedEvent;
import com.facturationpme.payments.gateway.CheckoutSession;
import com.facturationpme.payments.gateway.PaymentGatewayName;
import com.facturationpme.payments.gateway.PaymentGatewayProvider;
import com.facturationpme.payments.gateway.PaymentGatewayRegistry;
import com.facturationpme.payments.gateway.WebhookEvent;
import com.facturationpme.payments.gateway.WebhookOutcome;
import com.facturationpme.payments.mapper.PaymentMapper;
import com.facturationpme.payments.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private PaymentMapper paymentMapper;
  @Mock private DocumentNumberGenerator documentNumberGenerator;
  @Mock private PaymentGatewayRegistry paymentGatewayRegistry;
  @Mock private PaymentGatewayProvider paymentGatewayProvider;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private PaymentService paymentService;

  private UUID invoiceId;
  private Invoice invoice;
  private UUID paymentId;
  private Payment payment;

  @BeforeEach
  void setUp() {
    invoiceId = UUID.randomUUID();
    invoice =
        Invoice.builder()
            .id(invoiceId)
            .number("FAC-2026-0001")
            .clientId(UUID.randomUUID())
            .clientName("ACME Senegal SARL")
            .status(InvoiceStatus.SENT)
            .totalAmount(BigDecimal.valueOf(100000))
            .build();

    paymentId = UUID.randomUUID();
    payment =
        Payment.builder()
            .id(paymentId)
            .reference("PAY-2026-0001")
            .invoiceId(invoiceId)
            .invoiceNumber(invoice.getNumber())
            .clientId(invoice.getClientId())
            .clientName(invoice.getClientName())
            .amount(BigDecimal.valueOf(100000))
            .method(PaymentMethod.BANK_TRANSFER)
            .status(PaymentStatus.COMPLETED)
            .paidAt(Instant.now())
            .build();
  }

  private PaymentResponse dummyResponse(PaymentStatus status) {
    return new PaymentResponse(
        paymentId.toString(),
        payment.getReference(),
        invoiceId.toString(),
        invoice.getNumber(),
        invoice.getClientId().toString(),
        invoice.getClientName(),
        payment.getAmount(),
        payment.getMethod(),
        status,
        payment.getPaidAt(),
        null,
        null,
        null);
  }

  @Test
  void createShouldRejectUnknownInvoice() {
    PaymentCreateDto dto =
        new PaymentCreateDto(invoiceId, BigDecimal.TEN, PaymentMethod.CASH, Instant.now(), null);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.create(dto))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createFullPaymentShouldMarkInvoicePaid() {
    PaymentCreateDto dto =
        new PaymentCreateDto(
            invoiceId,
            BigDecimal.valueOf(100000),
            PaymentMethod.BANK_TRANSFER,
            Instant.now(),
            null);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(documentNumberGenerator.next(DocumentType.PAYMENT, "PAY")).thenReturn("PAY-2026-0002");
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(paymentRepository.sumCompletedAmountByInvoiceId(invoiceId))
        .thenReturn(BigDecimal.valueOf(100000));
    when(paymentMapper.toResponse(any(Payment.class)))
        .thenReturn(dummyResponse(PaymentStatus.COMPLETED));

    paymentService.create(dto);

    verify(invoiceRepository).save(invoice);
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    verify(eventPublisher).publishEvent(any(PaymentReceivedEvent.class));
  }

  @Test
  void createPartialPaymentShouldMarkInvoicePartiallyPaid() {
    PaymentCreateDto dto =
        new PaymentCreateDto(
            invoiceId, BigDecimal.valueOf(40000), PaymentMethod.CASH, Instant.now(), null);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(documentNumberGenerator.next(DocumentType.PAYMENT, "PAY")).thenReturn("PAY-2026-0002");
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(paymentRepository.sumCompletedAmountByInvoiceId(invoiceId))
        .thenReturn(BigDecimal.valueOf(40000));
    when(paymentMapper.toResponse(any(Payment.class)))
        .thenReturn(dummyResponse(PaymentStatus.COMPLETED));

    paymentService.create(dto);

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
  }

  @Test
  void createShouldNotAlterDraftOrCancelledInvoiceStatus() {
    invoice.setStatus(InvoiceStatus.CANCELLED);
    PaymentCreateDto dto =
        new PaymentCreateDto(
            invoiceId, BigDecimal.valueOf(40000), PaymentMethod.CASH, Instant.now(), null);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(documentNumberGenerator.next(DocumentType.PAYMENT, "PAY")).thenReturn("PAY-2026-0002");
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(paymentMapper.toResponse(any(Payment.class)))
        .thenReturn(dummyResponse(PaymentStatus.COMPLETED));

    paymentService.create(dto);

    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
    verify(invoiceRepository, never()).save(any(Invoice.class));
  }

  @Test
  void findByIdShouldThrowWhenMissing() {
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.findById(paymentId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void findByIdShouldReturnResponseWhenFound() {
    PaymentResponse expected = dummyResponse(PaymentStatus.COMPLETED);
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(paymentMapper.toResponse(payment)).thenReturn(expected);

    assertThat(paymentService.findById(paymentId)).isEqualTo(expected);
  }

  @Test
  void searchShouldMapRepositoryPageToResponsePage() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Payment> page = new PageImpl<>(List.of(payment));
    when(paymentRepository.findAll(ArgumentMatchers.<Specification<Payment>>any(), eq(pageable)))
        .thenReturn(page);
    when(paymentMapper.toResponse(payment)).thenReturn(dummyResponse(PaymentStatus.COMPLETED));

    Page<PaymentResponse> result = paymentService.search("acme", pageable);

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void refundShouldRejectNonCompletedPayment() {
    payment.setStatus(PaymentStatus.PENDING);
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> paymentService.refund(paymentId))
        .isInstanceOf(InvalidStateException.class);
  }

  @Test
  void refundShouldMarkPaymentRefundedAndDowngradeInvoiceStatus() {
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(paymentRepository.save(payment)).thenReturn(payment);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(paymentRepository.sumCompletedAmountByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
    when(paymentMapper.toResponse(payment)).thenReturn(dummyResponse(PaymentStatus.REFUNDED));

    paymentService.refund(paymentId);

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
    verify(eventPublisher).publishEvent(any(PaymentRefundedEvent.class));
  }

  @Test
  void initiateCheckoutShouldRejectFullyPaidInvoice() {
    CheckoutRequestDto dto =
        new CheckoutRequestDto(invoiceId, PaymentGatewayName.WAVE, "https://ok", "https://err");
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(paymentRepository.sumCompletedAmountByInvoiceId(invoiceId))
        .thenReturn(BigDecimal.valueOf(100000));

    assertThatThrownBy(() -> paymentService.initiateCheckout(dto))
        .isInstanceOf(InvalidStateException.class);
  }

  @Test
  void initiateCheckoutShouldCreatePendingPaymentAndReturnRedirectUrl() {
    CheckoutRequestDto dto =
        new CheckoutRequestDto(invoiceId, PaymentGatewayName.WAVE, "https://ok", "https://err");
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(paymentRepository.sumCompletedAmountByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
    when(paymentGatewayRegistry.resolve(PaymentGatewayName.WAVE))
        .thenReturn(paymentGatewayProvider);
    when(paymentGatewayProvider.initiateCheckout(any()))
        .thenReturn(new CheckoutSession("cos-123", "https://pay.wave.com/xyz"));
    when(documentNumberGenerator.next(DocumentType.PAYMENT, "PAY")).thenReturn("PAY-2026-0002");
    ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
    when(paymentRepository.save(captor.capture()))
        .thenAnswer(
            invocation -> {
              Payment argument = invocation.getArgument(0);
              argument.setId(UUID.randomUUID());
              return argument;
            });

    CheckoutResponseDto response = paymentService.initiateCheckout(dto);

    assertThat(response.checkoutUrl()).isEqualTo("https://pay.wave.com/xyz");
    Payment persisted = captor.getValue();
    assertThat(persisted.getStatus()).isEqualTo(PaymentStatus.PENDING);
    assertThat(persisted.getMethod()).isEqualTo(PaymentMethod.MOBILE_MONEY);
    assertThat(persisted.getGatewayProvider()).isEqualTo("WAVE");
    assertThat(persisted.getGatewaySessionId()).isEqualTo("cos-123");
    assertThat(persisted.getAmount()).isEqualByComparingTo("100000");
  }

  @Test
  void handleWebhookShouldCompletePendingPaymentOnSuccess() {
    payment.setStatus(PaymentStatus.PENDING);
    when(paymentGatewayRegistry.resolve(PaymentGatewayName.WAVE))
        .thenReturn(paymentGatewayProvider);
    when(paymentGatewayProvider.parseWebhook("body", Map.of()))
        .thenReturn(new WebhookEvent("cos-123", WebhookOutcome.SUCCEEDED));
    when(paymentRepository.findByGatewaySessionId("cos-123")).thenReturn(Optional.of(payment));
    when(paymentRepository.save(payment)).thenReturn(payment);
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(paymentRepository.sumCompletedAmountByInvoiceId(invoiceId))
        .thenReturn(BigDecimal.valueOf(100000));

    paymentService.handleWebhook(PaymentGatewayName.WAVE, "body", Map.of());

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    verify(eventPublisher).publishEvent(any(PaymentReceivedEvent.class));
  }

  @Test
  void handleWebhookShouldMarkPaymentFailedOnFailureOutcome() {
    payment.setStatus(PaymentStatus.PENDING);
    when(paymentGatewayRegistry.resolve(PaymentGatewayName.WAVE))
        .thenReturn(paymentGatewayProvider);
    when(paymentGatewayProvider.parseWebhook("body", Map.of()))
        .thenReturn(new WebhookEvent("cos-123", WebhookOutcome.FAILED));
    when(paymentRepository.findByGatewaySessionId("cos-123")).thenReturn(Optional.of(payment));
    when(paymentRepository.save(payment)).thenReturn(payment);

    paymentService.handleWebhook(PaymentGatewayName.WAVE, "body", Map.of());

    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    verify(invoiceRepository, never()).findById(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void handleWebhookShouldBeIdempotentForAlreadyFinalizedPayment() {
    payment.setStatus(PaymentStatus.COMPLETED);
    when(paymentGatewayRegistry.resolve(PaymentGatewayName.WAVE))
        .thenReturn(paymentGatewayProvider);
    when(paymentGatewayProvider.parseWebhook("body", Map.of()))
        .thenReturn(new WebhookEvent("cos-123", WebhookOutcome.SUCCEEDED));
    when(paymentRepository.findByGatewaySessionId("cos-123")).thenReturn(Optional.of(payment));

    paymentService.handleWebhook(PaymentGatewayName.WAVE, "body", Map.of());

    verify(paymentRepository, never()).save(any(Payment.class));
    verify(paymentRepository, times(0)).sumCompletedAmountByInvoiceId(any());
  }

  @Test
  void handleWebhookShouldThrowWhenSessionUnknown() {
    when(paymentGatewayRegistry.resolve(PaymentGatewayName.WAVE))
        .thenReturn(paymentGatewayProvider);
    when(paymentGatewayProvider.parseWebhook("body", Map.of()))
        .thenReturn(new WebhookEvent("unknown-session", WebhookOutcome.SUCCEEDED));
    when(paymentRepository.findByGatewaySessionId("unknown-session")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> paymentService.handleWebhook(PaymentGatewayName.WAVE, "body", Map.of()))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
