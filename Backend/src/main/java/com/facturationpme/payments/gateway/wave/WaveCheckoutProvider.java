package com.facturationpme.payments.gateway.wave;

import com.facturationpme.common.exception.InvalidWebhookSignatureException;
import com.facturationpme.common.exception.PaymentGatewayUnavailableException;
import com.facturationpme.payments.gateway.CheckoutRequest;
import com.facturationpme.payments.gateway.CheckoutSession;
import com.facturationpme.payments.gateway.PaymentGatewayName;
import com.facturationpme.payments.gateway.PaymentGatewayProvider;
import com.facturationpme.payments.gateway.WebhookEvent;
import com.facturationpme.payments.gateway.WebhookOutcome;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Integration reelle Wave Business Checkout (base URL, endpoint et authentification verifies via
 * docs.wave.com/business le 2026-07-10). La forme exacte du payload webhook (voir {@link
 * WaveWebhookPayload}) n'a en revanche pas pu etre confirmee publiquement - a valider contre le
 * tableau de bord developpeur Wave avant mise en production.
 */
@Component
public class WaveCheckoutProvider implements PaymentGatewayProvider {

  private static final String SIGNATURE_HEADER = "Wave-Signature";
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private final RestClient restClient;
  private final WaveProperties properties;
  private final ObjectMapper objectMapper;

  public WaveCheckoutProvider(
      RestClient.Builder restClientBuilder, WaveProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.restClient =
        restClientBuilder
            .baseUrl(properties.baseUrl())
            .defaultHeader("Authorization", "Bearer " + properties.apiKey())
            .build();
  }

  @Override
  public PaymentGatewayName name() {
    return PaymentGatewayName.WAVE;
  }

  @Override
  public CheckoutSession initiateCheckout(CheckoutRequest request) {
    requireConfigured();

    WaveCheckoutSessionRequest body =
        new WaveCheckoutSessionRequest(
            request.amount().setScale(0, RoundingMode.HALF_UP).toPlainString(),
            request.currency(),
            request.successUrl(),
            request.errorUrl());

    WaveCheckoutSessionResponse response =
        restClient
            .post()
            .uri("/v1/checkout/sessions")
            .body(body)
            .retrieve()
            .body(WaveCheckoutSessionResponse.class);

    return new CheckoutSession(response.id(), response.waveLaunchUrl());
  }

  @Override
  public WebhookEvent parseWebhook(String rawBody, Map<String, String> headers) {
    requireConfigured();
    String signature = headers.get(SIGNATURE_HEADER);
    if (signature == null || signature.isBlank() || !isSignatureValid(rawBody, signature)) {
      throw new InvalidWebhookSignatureException(
          "En-tete " + SIGNATURE_HEADER + " absent ou invalide.");
    }

    try {
      WaveWebhookPayload payload = objectMapper.readValue(rawBody, WaveWebhookPayload.class);
      WebhookOutcome outcome =
          "checkout.session.completed".equals(payload.type())
              ? WebhookOutcome.SUCCEEDED
              : WebhookOutcome.FAILED;
      return new WebhookEvent(payload.data().id(), outcome);
    } catch (Exception e) {
      throw new InvalidWebhookSignatureException("Payload webhook Wave illisible.");
    }
  }

  private boolean isSignatureValid(String rawBody, String signatureHeader) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(
          new SecretKeySpec(
              properties.webhookSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
      byte[] computed = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
      String computedHex = HexFormat.of().formatHex(computed);
      return java.security.MessageDigest.isEqual(
          computedHex.getBytes(StandardCharsets.UTF_8),
          signatureHeader.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException("Impossible de calculer la signature HMAC.", e);
    }
  }

  private void requireConfigured() {
    if (!properties.isConfigured()) {
      throw new PaymentGatewayUnavailableException(
          "Le fournisseur de paiement Wave n'est pas configure (WAVE_API_KEY manquant).");
    }
  }
}
