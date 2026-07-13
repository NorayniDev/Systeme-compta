package com.facturationpme.payments.gateway.wave;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Identifiants Wave Business (portail developpeur Wave) - jamais commit, fournis via variables
 * d'environnement ({@code WAVE_API_KEY}, {@code WAVE_WEBHOOK_SECRET}). Sans cles valides, {@link
 * WaveCheckoutProvider} echoue explicitement plutot que d'envoyer une requete vouee a l'echec.
 */
@ConfigurationProperties(prefix = "app.payment.wave")
public record WaveProperties(String baseUrl, String apiKey, String webhookSecret) {

  public boolean isConfigured() {
    return apiKey != null && !apiKey.isBlank();
  }
}
