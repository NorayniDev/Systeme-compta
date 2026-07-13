package com.facturationpme.payments.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.facturationpme.common.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayRegistryTest {

  @Mock private PaymentGatewayProvider waveProvider;

  @Test
  void resolveShouldReturnMatchingProvider() {
    when(waveProvider.name()).thenReturn(PaymentGatewayName.WAVE);
    PaymentGatewayRegistry registry = new PaymentGatewayRegistry(List.of(waveProvider));

    assertThat(registry.resolve(PaymentGatewayName.WAVE)).isSameAs(waveProvider);
  }

  @Test
  void resolveShouldThrowWhenProviderMissing() {
    PaymentGatewayRegistry registry = new PaymentGatewayRegistry(List.of());

    assertThatThrownBy(() -> registry.resolve(PaymentGatewayName.ORANGE_MONEY))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
