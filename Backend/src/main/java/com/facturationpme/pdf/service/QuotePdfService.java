package com.facturationpme.pdf.service;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.quotes.domain.Quote;
import com.facturationpme.quotes.repository.QuoteRepository;
import com.facturationpme.settings.dto.CompanySettingsDto;
import com.facturationpme.settings.service.CompanySettingsService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Assemble les donnees d'un devis pour le gabarit {@code pdf/quote}. */
@Service
@RequiredArgsConstructor
public class QuotePdfService {

  private static final String TEMPLATE = "pdf/quote";

  private final QuoteRepository quoteRepository;
  private final ClientRepository clientRepository;
  private final CompanySettingsService companySettingsService;
  private final PdfRenderingService pdfRenderingService;

  @Transactional(readOnly = true)
  public GeneratedPdf generate(UUID quoteId) {
    Quote quote =
        quoteRepository
            .findById(quoteId)
            .orElseThrow(() -> ResourceNotFoundException.of("Devis", quoteId));
    Client client = clientRepository.findById(quote.getClientId()).orElse(null);
    CompanySettingsDto company = companySettingsService.get();

    Map<String, Object> variables = new HashMap<>();
    variables.put("company", company);
    variables.put("quote", quote);
    variables.put("client", client);
    byte[] content = pdfRenderingService.render(TEMPLATE, variables);
    return new GeneratedPdf(content, quote.getNumber() + ".pdf");
  }
}
