package com.facturationpme.common.exception;

/**
 * Fournisseur de paiement en ligne non configure ou non implemente (ex: Wave sans cles API, Orange
 * Money en attente d'integration) - un etat attendu et permanent tant que les identifiants ne sont
 * pas fournis, pas une panne imprevue : mappee en 503, jamais en 500 generique.
 */
public class PaymentGatewayUnavailableException extends RuntimeException {

  public PaymentGatewayUnavailableException(String message) {
    super(message);
  }
}
