package com.facturationpme.dashboard.web;

import com.facturationpme.dashboard.dto.DashboardKpisResponse;
import com.facturationpme.dashboard.dto.InvoiceStatusBreakdownResponse;
import com.facturationpme.dashboard.dto.RecentActivityResponse;
import com.facturationpme.dashboard.dto.RevenueChartPointResponse;
import com.facturationpme.dashboard.service.DashboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Accessible a tout utilisateur authentifie, quel que soit son role - aucune permission dediee cote
 * frontend (voir nav-items.ts : l'entree "dashboard" ne porte aucune {@code permission}).
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/kpis")
  public DashboardKpisResponse kpis() {
    return dashboardService.getKpis();
  }

  @GetMapping("/revenue-chart")
  public List<RevenueChartPointResponse> revenueChart() {
    return dashboardService.getRevenueChart();
  }

  @GetMapping("/revenue-chart/status")
  public List<InvoiceStatusBreakdownResponse> invoiceStatusBreakdown() {
    return dashboardService.getInvoiceStatusBreakdown();
  }

  @GetMapping("/recent-activity")
  public List<RecentActivityResponse> recentActivity() {
    return dashboardService.getRecentActivity();
  }
}
