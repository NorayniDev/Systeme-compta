package com.facturationpme.payments.gateway.orangemoney;

import com.facturationpme.common.exception.PaymentGatewayUnavailableException;
import com.facturationpme.payments.gateway.CheckoutRequest;
import com.facturationpme.payments.gateway.CheckoutSession;
import com.facturationpme.payments.gateway.PaymentGatewayName;
import com.facturationpme.payments.gateway.PaymentGatewayProvider;
import com.facturationpme.payments.gateway.WebhookEvent;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Integration Orange Money (Sonatel) - NON IMPLEMENTEE.
 *
 * <p>developer.orange-sonatel.com confirme l'existence des produits QR Code, Cash In et
 * Notification, une authentification par token OAuth et un environnement de test gratuit, mais les
 * schemas exacts de requete/reponse et les chemins d'API sont derriere un compte developpeur
 * (section "Documentation technique") non accessible publiquement au moment de l'ecriture de ce
 * code.
 *
 * <p>Pour completer cette integration :
 *
 * <ol>
 *   <li>Creer un compte developpeur sur developer.orange-sonatel.com et recuperer les identifiants
 *       OAuth2 (client_id/client_secret) ainsi que la documentation technique (probablement "Web
 *       Payment" ou "Merchant Payment API").
 *   <li>Implementer le flux OAuth2 client credentials pour obtenir un token d'acces (typiquement
 *       {@code POST /oauth/token}).
 *   <li>Renseigner {@code app.payment.orange-money.*} dans application.yml (a l'image de {@code
 *       app.payment.wave.*}) et une classe {@code OrangeMoneyProperties} equivalente a {@code
 *       WaveProperties}.
 *   <li>Implementer {@link #initiateCheckout} et {@link #parseWebhook} en suivant le meme contrat
 *       que {@code WaveCheckoutProvider}, y compris la verification de signature du webhook.
 * </ol>
 */
@Component
public class OrangeMoneyProvider implements PaymentGatewayProvider {

  @Override
  public PaymentGatewayName name() {
    return PaymentGatewayName.ORANGE_MONEY;
  }

  @Override
  public CheckoutSession initiateCheckout(CheckoutRequest request) {
    throw new PaymentGatewayUnavailableException(
        "Integration Orange Money non implementee - identifiants et documentation technique "
            + "requis (compte developpeur sur developer.orange-sonatel.com). Voir la Javadoc de "
            + "cette classe pour les etapes.");
  }

  @Override
  public WebhookEvent parseWebhook(String rawBody, Map<String, String> headers) {
    throw new PaymentGatewayUnavailableException("Integration Orange Money non implementee.");
  }
}
