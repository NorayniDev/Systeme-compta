package com.facturationpme.reports.web;

import com.facturationpme.reports.dto.AgingReceivableLine;
import com.facturationpme.reports.dto.QuoteFunnelLine;
import com.facturationpme.reports.dto.SalesByClientLine;
import com.facturationpme.reports.service.ReportService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Integralement en lecture seule : voir {@code ReportService} pour le calcul des agregations. */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('report:read')")
public class ReportController {

  private final ReportService reportService;

  @GetMapping("/sales-by-client")
  public List<SalesByClientLine> salesByClient(
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    return reportService.getSalesByClient(startDate, endDate);
  }

  @GetMapping("/aging-receivables")
  public List<AgingReceivableLine> agingReceivables() {
    return reportService.getAgingReceivables();
  }

  @GetMapping("/quote-funnel")
  public List<QuoteFunnelLine> quoteFunnel() {
    return reportService.getQuoteFunnel();
  }
}
