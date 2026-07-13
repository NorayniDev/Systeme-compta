package com.facturationpme.dashboard.dto;

import java.math.BigDecimal;

/**
 * Alignee sur {@code IDashboardKpis} (features/dashboard/models/dashboard.model.ts). {@code
 * payables} et {@code productsSold} sont figes a zero : aucun sous-systeme de factures fournisseurs
 * (comptes fournisseurs) ni de lien facture-produit n'existe dans ce backend - voir {@code
 * DashboardService}.
 */
public record DashboardKpisResponse(
    BigDecimal revenue,
    BigDecimal revenueTrendPercent,
    long invoicesCount,
    BigDecimal invoicesTrendPercent,
    BigDecimal paymentsReceived,
    BigDecimal receivables,
    BigDecimal payables,
    long activeClients,
    long productsSold) {}
