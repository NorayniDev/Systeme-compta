package com.facturationpme.payments.event;

import com.facturationpme.payments.domain.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentReceivedEvent(
    UUID paymentId,
    String reference,
    String clientName,
    Instant paidAt,
    BigDecimal amount,
    PaymentMethod method) {}
