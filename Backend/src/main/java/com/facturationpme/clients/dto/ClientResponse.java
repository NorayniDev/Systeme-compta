package com.facturationpme.clients.dto;

import com.facturationpme.clients.domain.ClientStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** Aligne sur {@code IClient} (features/clients/models/client.model.ts). */
public record ClientResponse(
    String id,
    String name,
    String email,
    String phone,
    String address,
    String taxId,
    ClientStatus status,
    BigDecimal totalInvoiced,
    Instant createdAt,
    Instant updatedAt) {}
