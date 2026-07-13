import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe } from '@ngx-translate/core';
import { ReportService } from '../../services/report.service';
import {
  IAgingReceivableLine,
  IQuoteFunnelLine,
  ISalesByClientLine,
} from '../../models/report.model';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { formatDate } from '../../../../core/helpers/date.helper';
import { exportToCsv, exportToPdf } from '../../../../shared/helpers/table-export.helper';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';

/**
 * Page Rapports: trois vues d'analyse en lecture seule (ventes par client,
 * créances échues, entonnoir des devis), chacune exportable.
 */
@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatMenuModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    EmptyState,
    CurrencyXofPipe,
    HasPermissionDirective,
  ],
  templateUrl: './reports.html',
  styleUrl: './reports.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Reports implements OnInit {
  private readonly reportService = inject(ReportService);
  protected readonly Permission = Permission;
  protected readonly formatDate = formatDate;

  protected readonly salesColumns = [
    'clientName',
    'invoiceCount',
    'amountExclTax',
    'taxAmount',
    'totalAmount',
  ];
  protected readonly agingColumns = [
    'invoiceNumber',
    'clientName',
    'dueDate',
    'daysOverdue',
    'amountDue',
  ];
  protected readonly funnelColumns = ['status', 'count', 'totalAmount'];

  protected readonly isLoadingSales = signal(true);
  protected readonly salesByClient = signal<ISalesByClientLine[]>([]);

  protected readonly isLoadingAging = signal(true);
  protected readonly agingReceivables = signal<IAgingReceivableLine[]>([]);

  protected readonly isLoadingFunnel = signal(true);
  protected readonly quoteFunnel = signal<IQuoteFunnelLine[]>([]);

  ngOnInit(): void {
    this.reportService.getSalesByClient({}).subscribe((lines) => {
      this.salesByClient.set(lines);
      this.isLoadingSales.set(false);
    });

    this.reportService.getAgingReceivables().subscribe((lines) => {
      this.agingReceivables.set(lines);
      this.isLoadingAging.set(false);
    });

    this.reportService.getQuoteFunnel().subscribe((lines) => {
      this.quoteFunnel.set(lines);
      this.isLoadingFunnel.set(false);
    });
  }

  protected statusTranslationKey(status: string): string {
    return `quotes.status.${status}`;
  }

  exportSalesCsv(): void {
    exportToCsv('ventes-par-client', this.salesByClient(), [
      'clientName',
      'invoiceCount',
      'amountExclTax',
      'taxAmount',
      'totalAmount',
    ]);
  }

  exportAgingCsv(): void {
    exportToCsv('creances-echues', this.agingReceivables(), [
      'invoiceNumber',
      'clientName',
      'dueDate',
      'daysOverdue',
      'amountDue',
    ]);
  }

  exportFunnelCsv(): void {
    exportToCsv('devis-par-statut', this.quoteFunnel(), ['status', 'count', 'totalAmount']);
  }

  exportPdf(): void {
    exportToPdf();
  }
}
