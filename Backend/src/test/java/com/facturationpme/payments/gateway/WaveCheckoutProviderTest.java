package com.facturationpme.payments.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.facturationpme.common.exception.InvalidWebhookSignatureException;
import com.facturationpme.common.exception.PaymentGatewayUnavailableException;
import com.facturationpme.payments.gateway.wave.WaveCheckoutProvider;
import com.facturationpme.payments.gateway.wave.WaveProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class WaveCheckoutProviderTest {

  private static final String WEBHOOK_SECRET = "test-webhook-secret";

  private WaveCheckoutProvider waveCheckoutProvider;
  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    WaveProperties properties =
        new WaveProperties("https://api.wave.com", "test-api-key", WEBHOOK_SECRET);
    waveCheckoutProvider = new WaveCheckoutProvider(builder, properties, new ObjectMapper());
  }

  @Test
  void nameShouldBeWave() {
    assertThat(waveCheckoutProvider.name()).isEqualTo(PaymentGatewayName.WAVE);
  }

  @Test
  void initiateCheckoutShouldPostToWaveAndReturnLaunchUrl() {
    mockServer
        .expect(requestTo("https://api.wave.com/v1/checkout/sessions"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", "Bearer test-api-key"))
        .andRespond(
            withSuccess(
                "{\"id\":\"cos-1abc\",\"wave_launch_url\":\"https://pay.wave.com/xyz\"}",
                MediaType.APPLICATION_JSON));

    CheckoutSession session =
        waveCheckoutProvider.initiateCheckout(
            new CheckoutRequest(
                BigDecimal.valueOf(1200000), "XOF", "DEV-2026-0001", "https://ok", "https://err"));

    assertThat(session.sessionId()).isEqualTo("cos-1abc");
    assertThat(session.checkoutUrl()).isEqualTo("https://pay.wave.com/xyz");
    mockServer.verify();
  }

  @Test
  void initiateCheckoutShouldFailFastWhenNotConfigured() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer.bindTo(builder).build();
    WaveProperties unconfigured = new WaveProperties("https://api.wave.com", "", "");
    WaveCheckoutProvider provider =
        new WaveCheckoutProvider(builder, unconfigured, new ObjectMapper());

    assertThatThrownBy(
            () ->
                provider.initiateCheckout(
                    new CheckoutRequest(BigDecimal.TEN, "XOF", "ref", "https://ok", "https://err")))
        .isInstanceOf(PaymentGatewayUnavailableException.class);
  }

  @Test
  void parseWebhookShouldRejectMissingSignature() {
    assertThatThrownBy(() -> waveCheckoutProvider.parseWebhook("{}", Map.of()))
        .isInstanceOf(InvalidWebhookSignatureException.class);
  }

  @Test
  void parseWebhookShouldRejectInvalidSignature() {
    assertThatThrownBy(
            () ->
                waveCheckoutProvider.parseWebhook(
                    "{}", Map.of("Wave-Signature", "not-the-real-hmac")))
        .isInstanceOf(InvalidWebhookSignatureException.class);
  }

  @Test
  void parseWebhookShouldAcceptValidSignatureAndReportSucceeded() {
    String body = "{\"type\":\"checkout.session.completed\",\"data\":{\"id\":\"cos-1abc\"}}";
    String signature = computeHmac(body);

    WebhookEvent event =
        waveCheckoutProvider.parseWebhook(body, Map.of("Wave-Signature", signature));

    assertThat(event.sessionId()).isEqualTo("cos-1abc");
    assertThat(event.outcome()).isEqualTo(WebhookOutcome.SUCCEEDED);
  }

  @Test
  void parseWebhookShouldReportFailedForNonCompletedEventType() {
    String body = "{\"type\":\"checkout.session.payment_failed\",\"data\":{\"id\":\"cos-1abc\"}}";
    String signature = computeHmac(body);

    WebhookEvent event =
        waveCheckoutProvider.parseWebhook(body, Map.of("Wave-Signature", signature));

    assertThat(event.outcome()).isEqualTo(WebhookOutcome.FAILED);
  }

  private static String computeHmac(String body) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
