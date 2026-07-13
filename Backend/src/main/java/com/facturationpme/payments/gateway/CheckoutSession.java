package com.facturationpme.payments.gateway;

/** {@code sessionId} correle le futur webhook au paiement PENDING cree localement. */
public record CheckoutSession(String sessionId, String checkoutUrl) {}
