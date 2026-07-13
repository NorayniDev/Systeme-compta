package com.facturationpme.payments.dto;

import com.facturationpme.payments.domain.PaymentMethod;
import com.facturationpme.payments.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Aligne sur {@code IPayment} (features/payments/models/payment.model.ts). Ne comporte jamais les
 * champs internes de correlation avec un fournisseur de paiement en ligne ({@code
 * gatewayProvider}/{@code gatewaySessionId}).
 */
public record PaymentResponse(
    String id,
    String reference,
    String invoiceId,
    String invoiceNumber,
    String clientId,
    String clientName,
    BigDecimal amount,
    PaymentMethod method,
    PaymentStatus status,
    Instant paidAt,
    String notes,
    Instant createdAt,
    Instant updatedAt) {}
