package com.facturationpme.settings.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.event.AuditableActionEvent;
import com.facturationpme.common.security.SecurityUtils;
import com.facturationpme.settings.domain.CompanySettings;
import com.facturationpme.settings.dto.CompanySettingsDto;
import com.facturationpme.settings.repository.CompanySettingsRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configuration globale de l'entreprise - ligne singleton unique (voir migration V12). Source de
 * verite pour les prefixes de numerotation utilises par {@code InvoiceService}/{@code QuoteService}
 * : modifier {@code invoicePrefix}/{@code quotePrefix} ici a un effet reel sur la prochaine
 * facture/devis genere.
 */
@Service
@RequiredArgsConstructor
public class CompanySettingsService {

  private static final UUID SINGLETON_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private final CompanySettingsRepository companySettingsRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public CompanySettingsDto get() {
    return toDto(getSingleton());
  }

  @Transactional
  public CompanySettingsDto update(CompanySettingsDto dto) {
    CompanySettings settings = getSingleton();
    settings.setCompanyName(dto.companyName());
    settings.setAddress(dto.address());
    settings.setTaxId(dto.taxId());
    settings.setCurrency(dto.currency());
    settings.setDefaultTaxRate(dto.defaultTaxRate());
    settings.setInvoicePrefix(dto.invoicePrefix());
    settings.setQuotePrefix(dto.quotePrefix());
    CompanySettings saved = companySettingsRepository.save(settings);
    eventPublisher.publishEvent(
        new AuditableActionEvent(
            AuditAction.UPDATE,
            "Parametres entreprise",
            saved.getCompanyName(),
            SecurityUtils.currentUserId()));
    return toDto(saved);
  }

  @Transactional(readOnly = true)
  public String getInvoicePrefix() {
    return getSingleton().getInvoicePrefix();
  }

  @Transactional(readOnly = true)
  public String getQuotePrefix() {
    return getSingleton().getQuotePrefix();
  }

  private CompanySettings getSingleton() {
    return companySettingsRepository
        .findById(SINGLETON_ID)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Configuration de l'entreprise introuvable - migration V12 manquante."));
  }

  private static CompanySettingsDto toDto(CompanySettings settings) {
    return new CompanySettingsDto(
        settings.getCompanyName(),
        settings.getAddress(),
        settings.getTaxId(),
        settings.getCurrency(),
        settings.getDefaultTaxRate(),
        settings.getInvoicePrefix(),
        settings.getQuotePrefix());
  }
}
