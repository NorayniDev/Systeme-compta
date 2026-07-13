package com.facturationpme.payments.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.facturationpme.common.exception.PaymentGatewayUnavailableException;
import com.facturationpme.payments.gateway.orangemoney.OrangeMoneyProvider;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Confirme que le fournisseur echoue explicitement plutot que silencieusement tant qu'il n'est pas
 * implemente.
 */
class OrangeMoneyProviderTest {

  private final OrangeMoneyProvider orangeMoneyProvider = new OrangeMoneyProvider();

  @Test
  void nameShouldBeOrangeMoney() {
    assertThat(orangeMoneyProvider.name()).isEqualTo(PaymentGatewayName.ORANGE_MONEY);
  }

  @Test
  void initiateCheckoutShouldThrowUnsupportedOperation() {
    CheckoutRequest request =
        new CheckoutRequest(BigDecimal.TEN, "XOF", "ref", "https://ok", "https://err");

    assertThatThrownBy(() -> orangeMoneyProvider.initiateCheckout(request))
        .isInstanceOf(PaymentGatewayUnavailableException.class);
  }

  @Test
  void parseWebhookShouldThrowUnsupportedOperation() {
    assertThatThrownBy(() -> orangeMoneyProvider.parseWebhook("{}", Map.of()))
        .isInstanceOf(PaymentGatewayUnavailableException.class);
  }
}
