package com.facturationpme.payments.dto;

/** URL vers laquelle rediriger le client pour completer le paiement chez le fournisseur. */
public record CheckoutResponseDto(String paymentId, String checkoutUrl) {}
