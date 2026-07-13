package com.facturationpme.payments.gateway.wave;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Forme du payload webhook Wave (evenements {@code checkout.session.completed} / {@code
 * checkout.session.payment_failed}). A reconfirmer contre le tableau de bord developpeur Wave une
 * fois un compte reel disponible - non verifiable publiquement au moment de l'ecriture de ce code
 * (voir README backend).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record WaveWebhookPayload(String type, WaveWebhookData data) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  record WaveWebhookData(String id) {}
}
