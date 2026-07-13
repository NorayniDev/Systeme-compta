package com.facturationpme.payments.gateway;

import com.facturationpme.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Aiguille vers le {@link PaymentGatewayProvider} demande - ajouter un fournisseur ne touche pas
 * cette classe.
 */
@Component
public class PaymentGatewayRegistry {

  private final Map<PaymentGatewayName, PaymentGatewayProvider> providersByName;

  public PaymentGatewayRegistry(List<PaymentGatewayProvider> providers) {
    this.providersByName =
        providers.stream()
            .collect(Collectors.toMap(PaymentGatewayProvider::name, Function.identity()));
  }

  public PaymentGatewayProvider resolve(PaymentGatewayName name) {
    PaymentGatewayProvider provider = providersByName.get(name);
    if (provider == null) {
      throw new ResourceNotFoundException("Fournisseur de paiement inconnu : " + name);
    }
    return provider;
  }
}
