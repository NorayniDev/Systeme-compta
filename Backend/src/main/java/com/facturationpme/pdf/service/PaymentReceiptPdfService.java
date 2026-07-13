package com.facturationpme.pdf.service;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.common.exception.ResourceNotFoundException;
import com.facturationpme.invoices.domain.Invoice;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.payments.domain.Payment;
import com.facturationpme.payments.repository.PaymentRepository;
import com.facturationpme.settings.dto.CompanySettingsDto;
import com.facturationpme.settings.service.CompanySettingsService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Assemble les donnees d'un recu de paiement pour le gabarit {@code pdf/receipt}. */
@Service
@RequiredArgsConstructor
public class PaymentReceiptPdfService {

  private static final String TEMPLATE = "pdf/receipt";

  private final PaymentRepository paymentRepository;
  private final InvoiceRepository invoiceRepository;
  private final ClientRepository clientRepository;
  private final CompanySettingsService companySettingsService;
  private final PdfRenderingService pdfRenderingService;

  @Transactional(readOnly = true)
  public GeneratedPdf generate(UUID paymentId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> ResourceNotFoundException.of("Paiement", paymentId));
    Client client = clientRepository.findById(payment.getClientId()).orElse(null);
    Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
    CompanySettingsDto company = companySettingsService.get();

    Map<String, Object> variables = new HashMap<>();
    variables.put("company", company);
    variables.put("payment", payment);
    variables.put("client", client);
    variables.put("invoice", invoice);
    byte[] content = pdfRenderingService.render(TEMPLATE, variables);
    return new GeneratedPdf(content, "recu-" + payment.getReference() + ".pdf");
  }
}
