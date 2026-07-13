package com.facturationpme.common.exception;

/**
 * Signature cryptographique d'un webhook entrant invalide ou absente - jamais faire confiance a un
 * payload non verifie.
 */
public class InvalidWebhookSignatureException extends RuntimeException {

  public InvalidWebhookSignatureException(String message) {
    super(message);
  }
}
