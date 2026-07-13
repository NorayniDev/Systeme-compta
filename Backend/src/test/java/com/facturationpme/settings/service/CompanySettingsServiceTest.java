package com.facturationpme.settings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.facturationpme.settings.domain.CompanySettings;
import com.facturationpme.settings.dto.CompanySettingsDto;
import com.facturationpme.settings.repository.CompanySettingsRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CompanySettingsServiceTest {

  private static final UUID SINGLETON_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Mock private CompanySettingsRepository companySettingsRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private CompanySettingsService companySettingsService;

  private CompanySettings settings;

  @BeforeEach
  void setUp() {
    settings =
        CompanySettings.builder()
            .id(SINGLETON_ID)
            .companyName("FacturationPME SARL")
            .address("Rue 12, Plateau, Dakar, Senegal")
            .taxId("NINEA0011223")
            .currency("XOF")
            .defaultTaxRate(BigDecimal.valueOf(18))
            .invoicePrefix("FAC")
            .quotePrefix("DEV")
            .build();
  }

  @Test
  void getShouldReturnCurrentSingleton() {
    when(companySettingsRepository.findById(SINGLETON_ID)).thenReturn(Optional.of(settings));

    CompanySettingsDto dto = companySettingsService.get();

    assertThat(dto.companyName()).isEqualTo("FacturationPME SARL");
    assertThat(dto.invoicePrefix()).isEqualTo("FAC");
    assertThat(dto.quotePrefix()).isEqualTo("DEV");
  }

  @Test
  void getShouldThrowWhenSingletonRowMissing() {
    when(companySettingsRepository.findById(SINGLETON_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> companySettingsService.get())
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void updateShouldPersistNewValuesAndReturnThem() {
    CompanySettingsDto dto =
        new CompanySettingsDto(
            "Nouvelle Raison Sociale",
            "Nouvelle adresse",
            "NINEA9999999",
            "EUR",
            BigDecimal.valueOf(20),
            "INV",
            "QUO");
    when(companySettingsRepository.findById(SINGLETON_ID)).thenReturn(Optional.of(settings));
    when(companySettingsRepository.save(settings)).thenReturn(settings);

    CompanySettingsDto response = companySettingsService.update(dto);

    assertThat(response.companyName()).isEqualTo("Nouvelle Raison Sociale");
    assertThat(response.invoicePrefix()).isEqualTo("INV");
    assertThat(response.quotePrefix()).isEqualTo("QUO");
    assertThat(settings.getCurrency()).isEqualTo("EUR");
  }

  @Test
  void getInvoicePrefixShouldReturnCurrentPrefix() {
    when(companySettingsRepository.findById(SINGLETON_ID)).thenReturn(Optional.of(settings));

    assertThat(companySettingsService.getInvoicePrefix()).isEqualTo("FAC");
  }

  @Test
  void getQuotePrefixShouldReturnCurrentPrefix() {
    when(companySettingsRepository.findById(SINGLETON_ID)).thenReturn(Optional.of(settings));

    assertThat(companySettingsService.getQuotePrefix()).isEqualTo("DEV");
  }
}
