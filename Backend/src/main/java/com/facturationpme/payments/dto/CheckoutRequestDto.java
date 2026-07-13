package com.facturationpme.payments.dto;

import com.facturationpme.payments.gateway.PaymentGatewayName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Requete d'initiation d'un paiement en ligne sur une facture. Le montant n'est jamais fourni par
 * l'appelant : il est calcule cote serveur comme le solde restant du (facture.totalAmount - somme
 * des paiements COMPLETED).
 */
public record CheckoutRequestDto(
    @NotNull UUID invoiceId,
    @NotNull PaymentGatewayName provider,
    @NotBlank String successUrl,
    @NotBlank String errorUrl) {}
