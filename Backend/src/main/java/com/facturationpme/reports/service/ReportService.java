package com.facturationpme.reports.service;

import com.facturationpme.invoices.dto.AgingReceivableProjection;
import com.facturationpme.invoices.dto.SalesByClientProjection;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.quotes.dto.QuoteFunnelProjection;
import com.facturationpme.quotes.repository.QuoteRepository;
import com.facturationpme.reports.dto.AgingReceivableLine;
import com.facturationpme.reports.dto.QuoteFunnelLine;
import com.facturationpme.reports.dto.SalesByClientLine;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rapports en lecture seule, calcules a la volee par agregation sur les factures et devis - aucune
 * donnee propre au module, contrairement a Accounting qui persiste ses ecritures.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

  private final InvoiceRepository invoiceRepository;
  private final QuoteRepository quoteRepository;

  public List<SalesByClientLine> getSalesByClient(LocalDate startDate, LocalDate endDate) {
    return invoiceRepository.aggregateSalesByClient(startDate, endDate).stream()
        .map(ReportService::toSalesByClientLine)
        .toList();
  }

  public List<AgingReceivableLine> getAgingReceivables() {
    LocalDate today = LocalDate.now();
    return invoiceRepository.findOverdueInvoices(today).stream()
        .map(projection -> toAgingReceivableLine(projection, today))
        .toList();
  }

  public List<QuoteFunnelLine> getQuoteFunnel() {
    return quoteRepository.aggregateByStatus().stream()
        .sorted(Comparator.comparingInt(projection -> projection.status().ordinal()))
        .map(ReportService::toQuoteFunnelLine)
        .toList();
  }

  private static SalesByClientLine toSalesByClientLine(SalesByClientProjection projection) {
    return new SalesByClientLine(
        projection.clientId(),
        projection.clientName(),
        projection.invoiceCount(),
        projection.amountExclTax(),
        projection.taxAmount(),
        projection.totalAmount());
  }

  private static AgingReceivableLine toAgingReceivableLine(
      AgingReceivableProjection projection, LocalDate today) {
    long daysOverdue = ChronoUnit.DAYS.between(projection.dueDate(), today);
    return new AgingReceivableLine(
        projection.invoiceId(),
        projection.invoiceNumber(),
        projection.clientName(),
        projection.dueDate(),
        daysOverdue,
        projection.amountDue());
  }

  private static QuoteFunnelLine toQuoteFunnelLine(QuoteFunnelProjection projection) {
    return new QuoteFunnelLine(
        projection.status().name(), projection.count(), projection.totalAmount());
  }
}
