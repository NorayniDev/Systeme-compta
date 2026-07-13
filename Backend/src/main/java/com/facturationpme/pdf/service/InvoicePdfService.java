package com.facturationpme.pdf.service;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.invoices.domain.Invoice;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.settings.dto.CompanySettingsDto;
import com.facturationpme.settings.service.CompanySettingsService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Assemble les donnees d'une facture pour le gabarit {@code pdf/invoice}. */
@Service
@RequiredArgsConstructor
public class InvoicePdfService {

  private static final String TEMPLATE = "pdf/invoice";

  private final InvoiceRepository invoiceRepository;
  private final ClientRepository clientRepository;
  private final CompanySettingsService companySettingsService;
  private final PdfRenderingService pdfRenderingService;

  @Transactional(readOnly = true)
  public GeneratedPdf generate(UUID invoiceId) {
    Invoice invoice =
        invoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> ResourceNotFoundException.of("Facture", invoiceId));
    // Le client peut avoir ete supprime independamment (aucune contrainte d'integrite ne l'empeche
    // aujourd'hui) : clientName reste toujours disponible sur la facture elle-meme en repli.
    Client client = clientRepository.findById(invoice.getClientId()).orElse(null);
    CompanySettingsDto company = companySettingsService.get();

    Map<String, Object> variables = new HashMap<>();
    variables.put("company", company);
    variables.put("invoice", invoice);
    variables.put("client", client);
    byte[] content = pdfRenderingService.render(TEMPLATE, variables);
    return new GeneratedPdf(content, invoice.getNumber() + ".pdf");
  }
}
