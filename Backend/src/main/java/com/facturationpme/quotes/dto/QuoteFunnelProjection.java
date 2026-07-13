package com.facturationpme.quotes.dto;

import com.facturationpme.quotes.domain.QuoteStatus;
import java.math.BigDecimal;

/** Projection JPQL agregeant les devis par statut - support du rapport Devis par statut. */
public record QuoteFunnelProjection(QuoteStatus status, Long count, BigDecimal totalAmount) {}
