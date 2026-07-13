import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { InvoiceService } from '../../services/invoice.service';
import { IInvoice, INVOICE_STATUS_ACCENT } from '../../models/invoice.model';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';
import { formatDate } from '../../../../core/helpers/date.helper';
import { NotificationService } from '../../../../core/services/notification.service';
import { downloadBlob } from '../../../../shared/helpers/table-export.helper';

/** Fiche de détail d'une facture: en-tête, lignes et totaux. */
@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTableModule,
    TranslatePipe,
    CurrencyXofPipe,
    HasPermissionDirective,
  ],
  templateUrl: './invoice-detail.html',
  styleUrl: './invoice-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InvoiceDetail implements OnInit {
  private readonly invoiceService = inject(InvoiceService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);

  protected readonly Permission = Permission;
  protected readonly statusAccent = INVOICE_STATUS_ACCENT;
  protected readonly formatDate = formatDate;
  protected readonly lineColumns = ['description', 'quantity', 'unitPrice', 'taxRate', 'lineTotal'];
  protected readonly isLoading = signal(true);
  protected readonly isDownloading = signal(false);
  protected readonly invoice = signal<IInvoice | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.invoiceService.getById(id).subscribe((invoice) => {
      this.invoice.set(invoice);
      this.isLoading.set(false);
    });
  }

  downloadPdf(): void {
    const current = this.invoice();
    if (!current) return;

    this.isDownloading.set(true);
    this.invoiceService.downloadPdf(current.id).subscribe({
      next: (blob) => {
        downloadBlob(blob, `${current.number}.pdf`);
        this.isDownloading.set(false);
      },
      error: () => {
        this.notificationService.error(this.translateService.instant('common.downloadError'));
        this.isDownloading.set(false);
      },
    });
  }
}
