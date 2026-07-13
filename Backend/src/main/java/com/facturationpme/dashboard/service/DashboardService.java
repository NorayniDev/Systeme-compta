package com.facturationpme.dashboard.service;

import com.facturationpme.audit.domain.AuditAction;
import com.facturationpme.audit.domain.AuditLog;
import com.facturationpme.audit.repository.AuditLogRepository;
import com.facturationpme.clients.domain.ClientStatus;
import com.facturationpme.clients.repository.ClientRepository;
import com.facturationpme.dashboard.dto.DashboardKpisResponse;
import com.facturationpme.dashboard.dto.InvoiceStatusBreakdownResponse;
import com.facturationpme.dashboard.dto.RecentActivityResponse;
import com.facturationpme.dashboard.dto.RevenueChartPointResponse;
import com.facturationpme.invoices.domain.InvoiceStatus;
import com.facturationpme.invoices.dto.InvoiceMonthlyAmountProjection;
import com.facturationpme.invoices.dto.InvoiceStatusProjection;
import com.facturationpme.invoices.repository.InvoiceRepository;
import com.facturationpme.payments.repository.PaymentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tableau de bord accessible a tout utilisateur authentifie (aucune permission dediee cote
 * frontend). {@code payables} (comptes fournisseurs) et {@code productsSold} (lien facture-produit)
 * n'ont aucune source de donnees reelle dans ce backend et sont figes a zero plutot que fabriques -
 * voir les DTOs correspondants.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

  private static final Set<InvoiceStatus> OUTSTANDING_STATUSES =
      EnumSet.of(InvoiceStatus.SENT, InvoiceStatus.PARTIALLY_PAID);
  private static final int REVENUE_CHART_MONTHS = 6;
  private static final int PERCENT_SCALE = 2;
  private static final String[] FRENCH_MONTH_ABBREVIATIONS = {
    "Jan", "Fev", "Mar", "Avr", "Mai", "Juin", "Juil", "Aout", "Sep", "Oct", "Nov", "Dec"
  };

  private final InvoiceRepository invoiceRepository;
  private final PaymentRepository paymentRepository;
  private final ClientRepository clientRepository;
  private final AuditLogRepository auditLogRepository;

  public DashboardKpisResponse getKpis() {
    LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
    LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
    LocalDate nextMonthStart = currentMonthStart.plusMonths(1);

    BigDecimal currentRevenue =
        invoiceRepository.sumRevenueBetween(currentMonthStart, nextMonthStart);
    BigDecimal previousRevenue =
        invoiceRepository.sumRevenueBetween(previousMonthStart, currentMonthStart);

    long currentInvoiceCount =
        invoiceRepository.countByIssueDateGreaterThanEqualAndIssueDateLessThan(
            currentMonthStart, nextMonthStart);
    long previousInvoiceCount =
        invoiceRepository.countByIssueDateGreaterThanEqualAndIssueDateLessThan(
            previousMonthStart, currentMonthStart);

    Instant currentMonthStartInstant = currentMonthStart.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant nextMonthStartInstant = nextMonthStart.atStartOfDay(ZoneOffset.UTC).toInstant();
    BigDecimal paymentsReceived =
        paymentRepository.sumCompletedAmountPaidBetween(
            currentMonthStartInstant, nextMonthStartInstant);

    BigDecimal outstandingInvoiceAmount = invoiceRepository.sumOutstandingInvoiceAmount();
    BigDecimal paidAgainstOutstanding =
        paymentRepository.sumCompletedAmountForOutstandingInvoices();
    BigDecimal receivables = outstandingInvoiceAmount.subtract(paidAgainstOutstanding);

    long activeClients = clientRepository.countByStatus(ClientStatus.ACTIVE);

    return new DashboardKpisResponse(
        currentRevenue,
        trendPercent(currentRevenue, previousRevenue),
        invoiceRepository.count(),
        trendPercent(
            BigDecimal.valueOf(currentInvoiceCount), BigDecimal.valueOf(previousInvoiceCount)),
        paymentsReceived,
        receivables,
        BigDecimal.ZERO,
        activeClients,
        0L);
  }

  public List<RevenueChartPointResponse> getRevenueChart() {
    YearMonth currentMonth = YearMonth.now();
    YearMonth firstMonth = currentMonth.minusMonths(REVENUE_CHART_MONTHS - 1);
    LocalDate rangeStart = firstMonth.atDay(1);

    Map<YearMonth, BigDecimal> revenueByMonth = new LinkedHashMap<>();
    for (int i = 0; i < REVENUE_CHART_MONTHS; i++) {
      revenueByMonth.put(firstMonth.plusMonths(i), BigDecimal.ZERO);
    }
    for (InvoiceMonthlyAmountProjection projection :
        invoiceRepository.findAmountsIssuedSince(rangeStart)) {
      YearMonth month = YearMonth.from(projection.issueDate());
      revenueByMonth.merge(month, projection.totalAmount(), BigDecimal::add);
    }

    return revenueByMonth.entrySet().stream()
        .map(
            entry ->
                new RevenueChartPointResponse(
                    FRENCH_MONTH_ABBREVIATIONS[entry.getKey().getMonthValue() - 1],
                    entry.getValue(),
                    BigDecimal.ZERO))
        .toList();
  }

  public List<InvoiceStatusBreakdownResponse> getInvoiceStatusBreakdown() {
    LocalDate today = LocalDate.now();
    Map<String, Long> countsByBucket = new LinkedHashMap<>();
    for (InvoiceStatusProjection projection : invoiceRepository.findAllStatusesAndDueDates()) {
      String bucket = bucketFor(projection, today);
      countsByBucket.merge(bucket, 1L, Long::sum);
    }
    return countsByBucket.entrySet().stream()
        .map(entry -> new InvoiceStatusBreakdownResponse(entry.getKey(), entry.getValue()))
        .toList();
  }

  public List<RecentActivityResponse> getRecentActivity() {
    return auditLogRepository.findTop10ByOrderByOccurredAtDesc().stream()
        .map(DashboardService::toRecentActivity)
        .toList();
  }

  private static String bucketFor(InvoiceStatusProjection projection, LocalDate today) {
    boolean isOverdue =
        OUTSTANDING_STATUSES.contains(projection.status()) && projection.dueDate().isBefore(today);
    return isOverdue ? InvoiceStatus.OVERDUE.name() : projection.status().name();
  }

  private static BigDecimal trendPercent(BigDecimal current, BigDecimal previous) {
    if (previous.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return current
        .subtract(previous)
        .divide(previous, PERCENT_SCALE + 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(PERCENT_SCALE, RoundingMode.HALF_UP);
  }

  private static RecentActivityResponse toRecentActivity(AuditLog log) {
    return new RecentActivityResponse(
        log.getId().toString(),
        toActivityType(log.getEntityType()),
        buildMessage(log),
        log.getOccurredAt());
  }

  private static String toActivityType(String entityType) {
    return switch (entityType) {
      case "Facture" -> "invoice";
      case "Paiement" -> "payment";
      case "Client" -> "client";
      case "Devis" -> "quote";
      default -> "system";
    };
  }

  private static String buildMessage(AuditLog log) {
    if (log.getAction() == AuditAction.LOGIN) {
      return log.getUserName() + " s'est connecte";
    }
    String verb =
        switch (log.getAction()) {
          case CREATE -> "cree";
          case UPDATE -> "modifie";
          case DELETE -> "supprime";
          case VALIDATE -> "valide";
          case EXPORT -> "exporte";
          case LOGIN -> "connecte";
        };
    String base = log.getEntityType() + " " + log.getEntityLabel() + " " + verb;
    return log.getDetails() != null ? base + " (" + log.getDetails() + ")" : base;
  }
}
