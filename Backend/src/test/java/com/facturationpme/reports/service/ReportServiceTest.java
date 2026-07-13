package com.facturationpme.reports.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.facturationpme.invoices.dto.AgingReceivableProjection;
import com.facturationpme.invoices.dto.SalesByClientProjection;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.quotes.domain.QuoteStatus;
import com.facturationpme.quotes.dto.QuoteFunnelProjection;
import com.facturationpme.quotes.repository.QuoteRepository;
import com.facturationpme.reports.dto.AgingReceivableLine;
import com.facturationpme.reports.dto.QuoteFunnelLine;
import com.facturationpme.reports.dto.SalesByClientLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private QuoteRepository quoteRepository;

  @InjectMocks private ReportService reportService;

  @Test
  void getSalesByClientShouldMapProjectionsAndForwardDateRange() {
    UUID clientId = UUID.randomUUID();
    LocalDate startDate = LocalDate.of(2026, 1, 1);
    LocalDate endDate = LocalDate.of(2026, 12, 31);
    when(invoiceRepository.aggregateSalesByClient(startDate, endDate))
        .thenReturn(
            List.of(
                new SalesByClientProjection(
                    clientId,
                    "ACME Senegal SARL",
                    3L,
                    BigDecimal.valueOf(300000),
                    BigDecimal.valueOf(54000),
                    BigDecimal.valueOf(354000))));

    List<SalesByClientLine> result = reportService.getSalesByClient(startDate, endDate);

    assertThat(result)
        .containsExactly(
            new SalesByClientLine(
                clientId,
                "ACME Senegal SARL",
                3L,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(54000),
                BigDecimal.valueOf(354000)));
  }

  @Test
  void getAgingReceivablesShouldComputeDaysOverdueFromDueDate() {
    UUID invoiceId = UUID.randomUUID();
    LocalDate today = LocalDate.now();
    LocalDate dueDate = today.minusDays(10);
    when(invoiceRepository.findOverdueInvoices(today))
        .thenReturn(
            List.of(
                new AgingReceivableProjection(
                    invoiceId,
                    "FAC-2026-0004",
                    "Baobab Distribution",
                    dueDate,
                    BigDecimal.valueOf(118000))));

    List<AgingReceivableLine> result = reportService.getAgingReceivables();

    assertThat(result)
        .containsExactly(
            new AgingReceivableLine(
                invoiceId,
                "FAC-2026-0004",
                "Baobab Distribution",
                dueDate,
                10,
                BigDecimal.valueOf(118000)));
  }

  @Test
  void getQuoteFunnelShouldMapStatusToNameAndSortByEnumOrdinal() {
    when(quoteRepository.aggregateByStatus())
        .thenReturn(
            List.of(
                new QuoteFunnelProjection(QuoteStatus.CONVERTED, 2L, BigDecimal.valueOf(200000)),
                new QuoteFunnelProjection(QuoteStatus.DRAFT, 5L, BigDecimal.valueOf(500000)),
                new QuoteFunnelProjection(QuoteStatus.SENT, 3L, BigDecimal.valueOf(300000))));

    List<QuoteFunnelLine> result = reportService.getQuoteFunnel();

    assertThat(result)
        .containsExactly(
            new QuoteFunnelLine("DRAFT", 5L, BigDecimal.valueOf(500000)),
            new QuoteFunnelLine("SENT", 3L, BigDecimal.valueOf(300000)),
            new QuoteFunnelLine("CONVERTED", 2L, BigDecimal.valueOf(200000)));
  }
}
