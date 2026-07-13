package com.facturationpme.payments.gateway;

import java.util.Map;

/**
 * Contrat commun a tout fournisseur de paiement en ligne (Wave, Orange Money, ...). Ajouter un
 * fournisseur = une nouvelle implementation, aucune modification de {@code PaymentService} ni du
 * controleur.
 */
public interface PaymentGatewayProvider {

  PaymentGatewayName name();

  /** Cree une session de paiement chez le fournisseur et renvoie l'URL de redirection client. */
  CheckoutSession initiateCheckout(CheckoutRequest request);

  /**
   * Verifie l'authenticite du webhook (signature cryptographique) et en extrait le resultat.
   *
   * @throws com.facturationpme.common.exception.InvalidWebhookSignatureException si la signature
   *     est absente ou invalide
   */
  WebhookEvent parseWebhook(String rawBody, Map<String, String> headers);
}
