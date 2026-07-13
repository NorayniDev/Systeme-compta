package com.facturationpme.suppliers.dto;

import com.facturationpme.suppliers.domain.SupplierStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** Aligne sur {@code ISupplier} (features/suppliers/models/supplier.model.ts). */
public record SupplierResponse(
    String id,
    String name,
    String email,
    String phone,
    String address,
    String taxId,
    SupplierStatus status,
    BigDecimal totalPurchased,
    Instant createdAt,
    Instant updatedAt) {}
