package com.facturationpme.services.dto;

import com.facturationpme.services.domain.ServiceItemStatus;
import com.facturationpme.services.domain.ServiceItemUnit;
import java.math.BigDecimal;
import java.time.Instant;

/** Aligne sur {@code IServiceItem} (features/services/models/service-item.model.ts). */
public record ServiceItemResponse(
    String id,
    String name,
    String code,
    String description,
    String category,
    BigDecimal unitPrice,
    BigDecimal taxRate,
    ServiceItemUnit unit,
    ServiceItemStatus status,
    Instant createdAt,
    Instant updatedAt) {}
