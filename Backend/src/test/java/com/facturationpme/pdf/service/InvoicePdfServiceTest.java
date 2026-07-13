package com.facturationpme.pdf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.domain.ClientStatus;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.invoices.domain.Invoice;
import com.facturationpme.invoices.domain.InvoiceLine;
import com.facturationpme.invoices.domain.InvoiceStatus;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.settings.dto.CompanySettingsDto;
import com.facturationpme.settings.service.CompanySettingsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Utilise un vrai {@code PdfRenderingService} (pas de mock) pointe sur les vrais gabarits {@code
 * templates/pdf/invoice.html} - seule facon de verifier que le gabarit reel compile et produit le
 * contenu attendu sans pouvoir visualiser le rendu.
 */
@ExtendWith(MockitoExtension.class)
class InvoicePdfServiceTest {

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ClientRepository clientRepository;
  @Mock private CompanySettingsService companySettingsService;

  private InvoicePdfService invoicePdfService;

  @BeforeEach
  void setUp() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");
    resolver.setSuffix(".html");
    resolver.setPrefix("templates/");
    TemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(resolver);

    invoicePdfService =
        new InvoicePdfService(
            invoiceRepository,
            clientRepository,
            companySettingsService,
            new PdfRenderingService(templateEngine));
  }

  @Test
  void generateShouldProduceAPdfContainingInvoiceAndClientData() throws Exception {
    UUID invoiceId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();

    List<InvoiceLine> lines = new ArrayList<>();
    lines.add(
        InvoiceLine.builder()
            .description("Prestation de conseil")
            .quantity(BigDecimal.valueOf(2))
            .unitPrice(BigDecimal.valueOf(150000))
            .taxRate(BigDecimal.valueOf(18))
            .lineTotal(BigDecimal.valueOf(354000))
            .lineOrder(0)
            .build());

    Invoice invoice =
        Invoice.builder()
            .id(invoiceId)
            .number("FAC-2026-0099")
            .clientId(clientId)
            .clientName("ACME Senegal SARL")
            .issueDate(LocalDate.of(2026, 7, 1))
            .dueDate(LocalDate.of(2026, 7, 31))
            .lines(lines)
            .amountExclTax(BigDecimal.valueOf(300000))
            .taxAmount(BigDecimal.valueOf(54000))
            .totalAmount(BigDecimal.valueOf(354000))
            .status(InvoiceStatus.SENT)
            .notes("Merci de regler avant la date d'echeance.")
            .build();

    Client client =
        Client.builder()
            .id(clientId)
            .name("ACME Senegal SARL")
            .address("Rue 10, Plateau, Dakar")
            .phone("+221 77 111 22 33")
            .email("contact@acme.sn")
            .taxId("NINEA0012233")
            .status(ClientStatus.ACTIVE)
            .totalInvoiced(BigDecimal.ZERO)
            .build();

    CompanySettingsDto company =
        new CompanySettingsDto(
            "FacturationPME SARL",
            "Rue 12, Plateau, Dakar",
            "NINEA0011223",
            "XOF",
            BigDecimal.valueOf(18),
            "FAC",
            "DEV");

    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(companySettingsService.get()).thenReturn(company);

    GeneratedPdf pdf = invoicePdfService.generate(invoiceId);

    assertThat(pdf.fileName()).isEqualTo("FAC-2026-0099.pdf");
    assertThat(pdf.content()).isNotEmpty();

    try (PDDocument document = PDDocument.load(pdf.content())) {
      String text = new PDFTextStripper().getText(document);
      assertThat(text).contains("FACTURE");
      assertThat(text).contains("FAC-2026-0099");
      assertThat(text).contains("ACME Senegal SARL");
      assertThat(text).contains("Prestation de conseil");
      assertThat(text).contains("FacturationPME SARL");
    }
  }
}
