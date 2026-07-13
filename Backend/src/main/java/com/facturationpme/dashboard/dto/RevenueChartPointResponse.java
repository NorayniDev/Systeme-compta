package com.facturationpme.dashboard.dto;

import java.math.BigDecimal;

/**
 * Alignee sur {@code IRevenueChartPoint}. {@code expenses} est fige a zero : aucun suivi des
 * depenses/achats fournisseurs n'existe dans ce backend - voir {@code DashboardService}.
 */
public record RevenueChartPointResponse(String month, BigDecimal revenue, BigDecimal expenses) {}
