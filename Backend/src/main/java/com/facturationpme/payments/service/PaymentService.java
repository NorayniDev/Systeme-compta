package com.facturationpme.payments.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.common.exception.InvalidStateException;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.common.numbering.DocumentNumberGenerator;
import com.facturationpme.common.numbering.DocumentType;
import com.facturationpme.common.security.SecurityUtils;
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
import com.facturationpme.payments.gateway.CheckoutRequest;
import com.facturationpme.payments.gateway.CheckoutSession;
import com.facturationpme.payments.gateway.PaymentGatewayName;
import com.facturationpme.payments.gateway.PaymentGatewayProvider;
import com.facturationpme.payments.gateway.PaymentGatewayRegistry;
import com.facturationpme.payments.gateway.WebhookEvent;
import com.facturationpme.payments.gateway.WebhookOutcome;
import com.facturationpme.payments.mapper.PaymentMapper;
import com.facturationpme.payments.repository.PaymentRepository;
import com.facturationpme.payments.repository.PaymentSpecifications;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private static final String PAYMENT_REFERENCE_PREFIX = "PAY";
  private static final String CURRENCY = "XOF";
  private static final Set<InvoiceStatus> STATUS_LOCKED_INVOICE_STATES =
      Set.of(InvoiceStatus.DRAFT, InvoiceStatus.CANCELLED);

  private final PaymentRepository paymentRepository;
  private final InvoiceRepository invoiceRepository;
  private final PaymentMapper paymentMapper;
  private final DocumentNumberGenerator documentNumberGenerator;
  private final PaymentGatewayRegistry paymentGatewayRegistry;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public Page<PaymentResponse> search(String query, Pageable pageable) {
    Specification<Payment> specification =
        Specification.where(PaymentSpecifications.matchingQuery(query));
    return paymentRepository.findAll(specification, pageable).map(paymentMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public PaymentResponse findById(UUID id) {
    return paymentMapper.toResponse(getOrThrow(id));
  }

  @Transactional
  public PaymentResponse create(PaymentCreateDto dto) {
    Invoice invoice = findInvoiceOrThrow(dto.invoiceId());

    Payment payment =
        Payment.builder()
            .reference(documentNumberGenerator.next(DocumentType.PAYMENT, PAYMENT_REFERENCE_PREFIX))
            .invoiceId(invoice.getId())
            .invoiceNumber(invoice.getNumber())
            .clientId(invoice.getClientId())
            .clientName(invoice.getClientName())
            .amount(dto.amount())
            .method(dto.method())
            .status(PaymentStatus.COMPLETED)
            .paidAt(dto.paidAt())
            .notes(dto.notes())
            .build();

    Payment saved = paymentRepository.save(payment);
    recalculateInvoicePaymentStatus(invoice.getId());
    publishPaymentReceived(saved);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.CREATE, "Paiement", saved.getReference(), SecurityUtils.currentUserId()));
    return paymentMapper.toResponse(saved);
  }

  @Transactional
  public PaymentResponse refund(UUID id) {
    Payment payment = getOrThrow(id);
    if (payment.getStatus() != PaymentStatus.COMPLETED) {
      throw new InvalidStateException(
          "Seul un paiement complete peut etre rembourse (statut actuel : "
              + payment.getStatus()
              + ").");
    }
    payment.setStatus(PaymentStatus.REFUNDED);
    Payment saved = paymentRepository.save(payment);
    recalculateInvoicePaymentStatus(payment.getInvoiceId());
    eventPublisher.publishEvent(
        new PaymentRefundedEvent(
            saved.getId(),
            saved.getReference(),
            saved.getClientName(),
            Instant.now(),
            saved.getAmount(),
            saved.getMethod()));
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE,
            "Paiement",
            saved.getReference(),
            "Remboursement",
            SecurityUtils.currentUserId()));
    return paymentMapper.toResponse(saved);
  }

  /** Initie un paiement en ligne (Wave/Orange Money) sur le solde restant de la facture. */
  @Transactional
  public CheckoutResponseDto initiateCheckout(CheckoutRequestDto dto) {
    Invoice invoice = findInvoiceOrThrow(dto.invoiceId());
    BigDecimal remaining = remainingBalance(invoice);
    if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidStateException("Cette facture est deja entierement payee.");
    }

    PaymentGatewayProvider provider = paymentGatewayRegistry.resolve(dto.provider());
    CheckoutSession session =
        provider.initiateCheckout(
            new CheckoutRequest(
                remaining, CURRENCY, invoice.getNumber(), dto.successUrl(), dto.errorUrl()));

    Payment payment =
        Payment.builder()
            .reference(documentNumberGenerator.next(DocumentType.PAYMENT, PAYMENT_REFERENCE_PREFIX))
            .invoiceId(invoice.getId())
            .invoiceNumber(invoice.getNumber())
            .clientId(invoice.getClientId())
            .clientName(invoice.getClientName())
            .amount(remaining)
            .method(PaymentMethod.MOBILE_MONEY)
            .status(PaymentStatus.PENDING)
            .paidAt(Instant.now())
            .gatewayProvider(dto.provider().name())
            .gatewaySessionId(session.sessionId())
            .build();

    Payment saved = paymentRepository.save(payment);
    return new CheckoutResponseDto(saved.getId().toString(), session.checkoutUrl());
  }

  /**
   * Traite un webhook de confirmation de paiement - idempotent : ignore un paiement deja finalise.
   */
  @Transactional
  public void handleWebhook(
      PaymentGatewayName providerName, String rawBody, Map<String, String> headers) {
    PaymentGatewayProvider provider = paymentGatewayRegistry.resolve(providerName);
    WebhookEvent event = provider.parseWebhook(rawBody, headers);

    Payment payment =
        paymentRepository
            .findByGatewaySessionId(event.sessionId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Paiement introuvable pour la session : " + event.sessionId()));

    if (payment.getStatus() != PaymentStatus.PENDING) {
      return;
    }

    if (event.outcome() == WebhookOutcome.SUCCEEDED) {
      payment.setStatus(PaymentStatus.COMPLETED);
      payment.setPaidAt(Instant.now());
      Payment saved = paymentRepository.save(payment);
      recalculateInvoicePaymentStatus(payment.getInvoiceId());
      publishPaymentReceived(saved);
    } else {
      payment.setStatus(PaymentStatus.FAILED);
      paymentRepository.save(payment);
    }
  }

  private void publishPaymentReceived(Payment payment) {
    eventPublisher.publishEvent(
        new PaymentReceivedEvent(
            payment.getId(),
            payment.getReference(),
            payment.getClientName(),
            payment.getPaidAt(),
            payment.getAmount(),
            payment.getMethod()));
  }

  private BigDecimal remainingBalance(Invoice invoice) {
    BigDecimal alreadyPaid = paymentRepository.sumCompletedAmountByInvoiceId(invoice.getId());
    return invoice.getTotalAmount().subtract(alreadyPaid);
  }

  private void recalculateInvoicePaymentStatus(UUID invoiceId) {
    Invoice invoice =
        invoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> ResourceNotFoundException.of("Facture", invoiceId));
    if (STATUS_LOCKED_INVOICE_STATES.contains(invoice.getStatus())) {
      return;
    }

    BigDecimal totalPaid = paymentRepository.sumCompletedAmountByInvoiceId(invoiceId);
    InvoiceStatus newStatus;
    if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
      newStatus = InvoiceStatus.PAID;
    } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
      newStatus = InvoiceStatus.PARTIALLY_PAID;
    } else {
      newStatus = InvoiceStatus.SENT;
    }
    invoice.setStatus(newStatus);
    invoiceRepository.save(invoice);
  }

  private Invoice findInvoiceOrThrow(UUID invoiceId) {
    return invoiceRepository
        .findById(invoiceId)
        .orElseThrow(() -> ResourceNotFoundException.of("Facture", invoiceId));
  }

  private Payment getOrThrow(UUID id) {
    return paymentRepository
        .findById(id)
        .orElseThrow(() -> ResourceNotFoundException.of("Paiement", id));
  }
}
