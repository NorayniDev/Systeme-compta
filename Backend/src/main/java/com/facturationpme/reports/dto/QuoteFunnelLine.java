package com.facturationpme.reports.dto;

import java.math.BigDecimal;

public record QuoteFunnelLine(String status, Long count, BigDecimal totalAmount) {}
