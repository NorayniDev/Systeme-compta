package com.facturationpme.pdf.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.facturationpme.clients.domain.Client;
import com.facturationpme.clients.domain.ClientStatus;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.quotes.domain.Quote;
import com.facturationpme.quotes.domain.QuoteLine;
import com.facturationpme.quotes.domain.QuoteStatus;
import com.facturationpme.quotes.repository.QuoteRepository;
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

@ExtendWith(MockitoExtension.class)
class QuotePdfServiceTest {

  @Mock private QuoteRepository quoteRepository;
  @Mock private ClientRepository clientRepository;
  @Mock private CompanySettingsService companySettingsService;

  private QuotePdfService quotePdfService;

  @BeforeEach
  void setUp() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");
    resolver.setSuffix(".html");
    resolver.setPrefix("templates/");
    TemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(resolver);

    quotePdfService =
        new QuotePdfService(
            quoteRepository,
            clientRepository,
            companySettingsService,
            new PdfRenderingService(templateEngine));
  }

  @Test
  void generateShouldProduceAPdfContainingQuoteAndClientData() throws Exception {
    UUID quoteId = UUID.randomUUID();
    UUID clientId = UUID.randomUUID();

    List<QuoteLine> lines = new ArrayList<>();
    lines.add(
        QuoteLine.builder()
            .description("Ordinateur portable")
            .quantity(BigDecimal.valueOf(4))
            .unitPrice(BigDecimal.valueOf(250000))
            .taxRate(BigDecimal.valueOf(18))
            .lineTotal(BigDecimal.valueOf(1180000))
            .lineOrder(0)
            .build());

    Quote quote =
        Quote.builder()
            .id(quoteId)
            .number("DEV-2026-0099")
            .clientId(clientId)
            .clientName("Baobab Distribution")
            .issueDate(LocalDate.of(2026, 6, 15))
            .validUntil(LocalDate.of(2026, 7, 15))
            .lines(lines)
            .amountExclTax(BigDecimal.valueOf(1000000))
            .taxAmount(BigDecimal.valueOf(180000))
            .totalAmount(BigDecimal.valueOf(1180000))
            .status(QuoteStatus.SENT)
            .build();

    Client client =
        Client.builder()
            .id(clientId)
            .name("Baobab Distribution")
            .address("Thies, Senegal")
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

    when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
    when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
    when(companySettingsService.get()).thenReturn(company);

    GeneratedPdf pdf = quotePdfService.generate(quoteId);

    assertThat(pdf.fileName()).isEqualTo("DEV-2026-0099.pdf");

    try (PDDocument document = PDDocument.load(pdf.content())) {
      String text = new PDFTextStripper().getText(document);
      assertThat(text).contains("DEVIS");
      assertThat(text).contains("DEV-2026-0099");
      assertThat(text).contains("Baobab Distribution");
      assertThat(text).contains("Ordinateur portable");
    }
  }
}
