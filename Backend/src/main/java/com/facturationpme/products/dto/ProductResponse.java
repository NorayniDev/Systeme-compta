package com.facturationpme.products.dto;

import com.facturationpme.products.domain.ProductStatus;
import com.facturationpme.products.domain.ProductUnit;
import java.math.BigDecimal;
import java.time.Instant;

/** Aligne sur {@code IProduct} (features/products/models/product.model.ts). */
public record ProductResponse(
    String id,
    String name,
    String sku,
    String description,
    String category,
    BigDecimal unitPrice,
    BigDecimal taxRate,
    ProductUnit unit,
    Integer stockQuantity,
    ProductStatus status,
    Instant createdAt,
    Instant updatedAt) {}
