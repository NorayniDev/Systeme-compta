import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { KpiCard } from '../../../../shared/widgets/kpi-card/kpi-card';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { DashboardService } from '../../services/dashboard.service';
import {
  IDashboardKpis,
  IInvoiceStatusBreakdown,
  IRecentActivity,
  IRevenueChartPoint,
} from '../../models/dashboard.model';
import { AuthService } from '../../../../core/authentication/auth.service';
import { getFullName } from '../../../../core/models/user.model';

const ACTIVITY_ICONS: Record<IRecentActivity['type'], string> = {
  invoice: 'receipt_long',
  payment: 'payments',
  client: 'person_add',
  quote: 'request_quote',
  system: 'info',
};

/**
 * Page tableau de bord: KPIs clés, tendance du chiffre d'affaires,
 * répartition des factures et flux d'activité récente.
 * Aucune logique métier ici — tout provient de `DashboardService`.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    BaseChartDirective,
    TranslatePipe,
    KpiCard,
    EmptyState,
    RelativeTimePipe,
    CurrencyXofPipe,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Dashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly authService = inject(AuthService);
  private readonly translateService = inject(TranslateService);
  protected readonly activityIcons = ACTIVITY_ICONS;

  protected readonly isLoading = signal(true);
  protected readonly kpis = signal<IDashboardKpis | null>(null);
  protected readonly recentActivity = signal<IRecentActivity[]>([]);

  protected readonly welcomeMessage = computed(() => {
    const user = this.authService.currentUser();
    return this.translateService.instant('dashboard.welcome', {
      name: user ? getFullName(user) : '',
    });
  });

  protected readonly revenueChartData = signal<ChartData<'line'>>({ labels: [], datasets: [] });
  protected readonly revenueChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } },
    scales: { y: { beginAtZero: true } },
  };

  protected readonly invoiceStatusChartData = signal<ChartData<'doughnut'>>({
    labels: [],
    datasets: [],
  });
  protected readonly invoiceStatusChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } },
  };

  ngOnInit(): void {
    forkJoin({
      kpis: this.dashboardService.getKpis().pipe(catchError(() => of(null))),
      revenue: this.dashboardService
        .getRevenueChart()
        .pipe(catchError(() => of([] as IRevenueChartPoint[]))),
      statuses: this.dashboardService
        .getInvoiceStatusBreakdown()
        .pipe(catchError(() => of([] as IInvoiceStatusBreakdown[]))),
      activity: this.dashboardService
        .getRecentActivity()
        .pipe(catchError(() => of([] as IRecentActivity[]))),
    }).subscribe(({ kpis, revenue, statuses, activity }) => {
      this.kpis.set(kpis);
      this.recentActivity.set(activity);
      this.revenueChartData.set(this.buildRevenueChart(revenue));
      this.invoiceStatusChartData.set(this.buildInvoiceStatusChart(statuses));
      this.isLoading.set(false);
    });
  }

  private buildRevenueChart(points: IRevenueChartPoint[]): ChartData<'line'> {
    return {
      labels: points.map((p) => p.month),
      datasets: [
        {
          label: this.translateService.instant('dashboard.kpi.revenue'),
          data: points.map((p) => p.revenue),
          fill: true,
          tension: 0.35,
          borderColor: '#1e88e5',
          backgroundColor: 'rgba(30, 136, 229, 0.15)',
        },
        {
          label: this.translateService.instant('dashboard.charts.expenses'),
          data: points.map((p) => p.expenses),
          fill: true,
          tension: 0.35,
          borderColor: '#43a047',
          backgroundColor: 'rgba(67, 160, 71, 0.12)',
        },
      ],
    };
  }

  private buildInvoiceStatusChart(statuses: IInvoiceStatusBreakdown[]): ChartData<'doughnut'> {
    return {
      labels: statuses.map((s) => this.translateService.instant(`invoices.status.${s.status}`)),
      datasets: [
        {
          data: statuses.map((s) => s.count),
          backgroundColor: ['#1e88e5', '#43a047', '#fb8c00', '#e53935', '#8e24aa', '#546e7a'],
        },
      ],
    };
  }
}
