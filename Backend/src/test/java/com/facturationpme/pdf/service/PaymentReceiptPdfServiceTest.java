package com.facturationpme.pdf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.domain.ClientStatus;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.invoices.domain.Invoice;
import com.facturationpme.invoices.domain.InvoiceStatus;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.payments.domain.Payment;
import com.facturationpme.payments.domain.PaymentMethod;
import com.facturationpme.payments.domain.PaymentStatus;
import com.facturationpme.payments.repository.PaymentRepository;
import com.facturationpme.settings.dto.CompanySettingsDto;
import com.facturationpme.settings.service.CompanySettingsService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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

@ExtendWith(MockitoExtension.class)
class PaymentReceiptPdfServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private ClientRepository clientRepository;
  @Mock private CompanySettingsService companySettingsService;

  private PaymentReceiptPdfService paymentReceiptPdfService;

  @BeforeEach
  void setUp() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");
    resolver.setSuffix(".html");
    resolver.setPrefix("templates/");
    TemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(resolver);

    paymentReceiptPdfService =
        new PaymentReceiptPdfService(
            paymentRepository,
            invoiceRepository,
            clientRepository,
            companySettingsService,
            new PdfRenderingService(templateEngine));
  }

  @Test
  void generateShouldProduceAPdfContainingPaymentAndInvoiceData() throws Exception {
    UUID paymentId = UUID.randomUUID();
    UUID invoiceId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();

    Payment payment =
        Payment.builder()
            .id(paymentId)
            .reference("PAY-2026-0099")
            .invoiceId(invoiceId)
            .invoiceNumber("FAC-2026-0099")
            .clientId(clientId)
            .clientName("ACME Senegal SARL")
            .amount(BigDecimal.valueOf(354000))
            .method(PaymentMethod.BANK_TRANSFER)
            .status(PaymentStatus.COMPLETED)
            .paidAt(Instant.parse("2026-07-10T09:00:00Z"))
            .build();

    Invoice invoice =
        Invoice.builder()
            .id(invoiceId)
            .number("FAC-2026-0099")
            .clientId(clientId)
            .clientName("ACME Senegal SARL")
            .issueDate(LocalDate.of(2026, 7, 1))
            .dueDate(LocalDate.of(2026, 7, 31))
            .lines(new ArrayList<>())
            .amountExclTax(BigDecimal.valueOf(300000))
            .taxAmount(BigDecimal.valueOf(54000))
            .totalAmount(BigDecimal.valueOf(354000))
            .status(InvoiceStatus.PAID)
            .build();

    Client client =
        Client.builder()
            .id(clientId)
            .name("ACME Senegal SARL")
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

    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
    when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(companySettingsService.get()).thenReturn(company);

    GeneratedPdf pdf = paymentReceiptPdfService.generate(paymentId);

    assertThat(pdf.fileName()).isEqualTo("recu-PAY-2026-0099.pdf");

    try (PDDocument document = PDDocument.load(pdf.content())) {
      String text = new PDFTextStripper().getText(document);
      assertThat(text).contains("RECU");
      assertThat(text).contains("PAY-2026-0099");
      assertThat(text).contains("ACME Senegal SARL");
      assertThat(text).contains("FAC-2026-0099");
      assertThat(text).contains("Virement bancaire");
    }
  }
}
