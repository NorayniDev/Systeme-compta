package com.facturationpme.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private ClientRepository clientRepository;
  @Mock private AuditLogRepository auditLogRepository;

  @InjectMocks private DashboardService dashboardService;

  @Test
  void getKpisShouldComputeTrendsAndReceivablesAndZeroOutUnavailableFields() {
    when(invoiceRepository.sumRevenueBetween(any(), any()))
        .thenReturn(BigDecimal.valueOf(200000))
        .thenReturn(BigDecimal.valueOf(100000));
    when(invoiceRepository.countByIssueDateGreaterThanEqualAndIssueDateLessThan(any(), any()))
        .thenReturn(10L)
        .thenReturn(5L);
    when(paymentRepository.sumCompletedAmountPaidBetween(any(Instant.class), any(Instant.class)))
        .thenReturn(BigDecimal.valueOf(150000));
    when(invoiceRepository.sumOutstandingInvoiceAmount()).thenReturn(BigDecimal.valueOf(500000));
    when(paymentRepository.sumCompletedAmountForOutstandingInvoices())
        .thenReturn(BigDecimal.valueOf(120000));
    when(invoiceRepository.count()).thenReturn(42L);
    when(clientRepository.countByStatus(ClientStatus.ACTIVE)).thenReturn(7L);

    DashboardKpisResponse response = dashboardService.getKpis();

    assertThat(response.revenue()).isEqualByComparingTo("200000");
    assertThat(response.revenueTrendPercent()).isEqualByComparingTo("100.00");
    assertThat(response.invoicesCount()).isEqualTo(42L);
    assertThat(response.invoicesTrendPercent()).isEqualByComparingTo("100.00");
    assertThat(response.paymentsReceived()).isEqualByComparingTo("150000");
    assertThat(response.receivables()).isEqualByComparingTo("380000");
    assertThat(response.payables()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.activeClients()).isEqualTo(7L);
    assertThat(response.productsSold()).isZero();
  }

  @Test
  void getKpisShouldReturnZeroTrendWhenPreviousPeriodHasNoData() {
    when(invoiceRepository.sumRevenueBetween(any(), any()))
        .thenReturn(BigDecimal.valueOf(50000))
        .thenReturn(BigDecimal.ZERO);
    when(invoiceRepository.countByIssueDateGreaterThanEqualAndIssueDateLessThan(any(), any()))
        .thenReturn(3L)
        .thenReturn(0L);
    when(paymentRepository.sumCompletedAmountPaidBetween(any(Instant.class), any(Instant.class)))
        .thenReturn(BigDecimal.ZERO);
    when(invoiceRepository.sumOutstandingInvoiceAmount()).thenReturn(BigDecimal.ZERO);
    when(paymentRepository.sumCompletedAmountForOutstandingInvoices()).thenReturn(BigDecimal.ZERO);
    when(invoiceRepository.count()).thenReturn(3L);
    when(clientRepository.countByStatus(ClientStatus.ACTIVE)).thenReturn(1L);

    DashboardKpisResponse response = dashboardService.getKpis();

    assertThat(response.revenueTrendPercent()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.invoicesTrendPercent()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void getRevenueChartShouldBucketByMonthAndDefaultMissingMonthsToZero() {
    YearMonth currentMonth = YearMonth.now();
    when(invoiceRepository.findAmountsIssuedSince(any()))
        .thenReturn(
            List.of(
                new InvoiceMonthlyAmountProjection(
                    currentMonth.atDay(5), BigDecimal.valueOf(100000)),
                new InvoiceMonthlyAmountProjection(
                    currentMonth.atDay(20), BigDecimal.valueOf(50000))));

    List<RevenueChartPointResponse> chart = dashboardService.getRevenueChart();

    assertThat(chart).hasSize(6);
    RevenueChartPointResponse currentMonthPoint = chart.get(5);
    assertThat(currentMonthPoint.revenue()).isEqualByComparingTo("150000");
    assertThat(currentMonthPoint.expenses()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(chart.get(0).revenue()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void getInvoiceStatusBreakdownShouldReclassifyOverdueInvoices() {
    LocalDate today = LocalDate.now();
    when(invoiceRepository.findAllStatusesAndDueDates())
        .thenReturn(
            List.of(
                new InvoiceStatusProjection(InvoiceStatus.SENT, today.minusDays(5)),
                new InvoiceStatusProjection(InvoiceStatus.SENT, today.plusDays(5)),
                new InvoiceStatusProjection(InvoiceStatus.PAID, today.minusDays(30)),
                new InvoiceStatusProjection(InvoiceStatus.PARTIALLY_PAID, today.minusDays(1))));

    List<InvoiceStatusBreakdownResponse> breakdown = dashboardService.getInvoiceStatusBreakdown();

    assertThat(breakdown)
        .containsExactlyInAnyOrder(
            new InvoiceStatusBreakdownResponse("OVERDUE", 2L),
            new InvoiceStatusBreakdownResponse("SENT", 1L),
            new InvoiceStatusBreakdownResponse("PAID", 1L));
  }

  @Test
  void getRecentActivityShouldMapEntityTypeAndBuildFrenchMessage() {
    AuditLog invoiceLog =
        AuditLog.builder()
            .id(java.util.UUID.randomUUID())
            .occurredAt(Instant.now())
            .userName("Admin Demo")
            .action(AuditAction.CREATE)
            .entityType("Facture")
            .entityLabel("FAC-2026-0099")
            .build();
    AuditLog loginLog =
        AuditLog.builder()
            .id(java.util.UUID.randomUUID())
            .occurredAt(Instant.now())
            .userName("Admin Demo")
            .action(AuditAction.LOGIN)
            .entityType("Auth")
            .entityLabel("Connexion")
            .build();
    when(auditLogRepository.findTop10ByOrderByOccurredAtDesc())
        .thenReturn(List.of(invoiceLog, loginLog));

    List<RecentActivityResponse> activity = dashboardService.getRecentActivity();

    assertThat(activity).hasSize(2);
    assertThat(activity.get(0).type()).isEqualTo("invoice");
    assertThat(activity.get(0).message()).isEqualTo("Facture FAC-2026-0099 cree");
    assertThat(activity.get(1).type()).isEqualTo("system");
    assertThat(activity.get(1).message()).isEqualTo("Admin Demo s'est connecte");
  }
}
