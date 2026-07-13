import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { InvoiceService } from '../../services/invoice.service';
import { IInvoice, INVOICE_STATUS_ACCENT, InvoiceStatus } from '../../models/invoice.model';
import { DEFAULT_PAGE_REQUEST, ISearchFilter } from '../../../../core/models/pagination.model';
import { APP_CONSTANTS } from '../../../../core/constants/app.constant';
import { NotificationService } from '../../../../core/services/notification.service';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';
import { exportToCsv, exportToPdf } from '../../../../shared/helpers/table-export.helper';
import { formatDate, isOverdue } from '../../../../core/helpers/date.helper';

/**
 * Liste des factures: recherche, tri, pagination côté serveur, export,
 * validation rapide et suppression avec confirmation.
 */
@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    EmptyState,
    CurrencyXofPipe,
    HasPermissionDirective,
  ],
  templateUrl: './invoice-list.html',
  styleUrl: './invoice-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InvoiceList implements OnInit {
  private readonly invoiceService = inject(InvoiceService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly dialog = inject(MatDialog);

  protected readonly Permission = Permission;
  protected readonly statusAccent = INVOICE_STATUS_ACCENT;
  protected readonly formatDate = formatDate;
  protected readonly isOverdue = isOverdue;
  protected readonly displayedColumns = [
    'number',
    'clientName',
    'issueDate',
    'dueDate',
    'totalAmount',
    'status',
    'actions',
  ];
  protected readonly pageSizeOptions = APP_CONSTANTS.PAGE_SIZE_OPTIONS;

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly isLoading = signal(true);
  protected readonly invoices = signal<IInvoice[]>([]);
  protected readonly totalElements = signal(0);

  private filter: ISearchFilter = { ...DEFAULT_PAGE_REQUEST, size: this.pageSizeOptions[0] };

  ngOnInit(): void {
    this.loadInvoices();

    this.searchControl.valueChanges
      .pipe(debounceTime(APP_CONSTANTS.DEBOUNCE_SEARCH_MS), distinctUntilChanged())
      .subscribe((query) => {
        this.filter = { ...this.filter, query, page: 0 };
        this.loadInvoices();
      });
  }

  onPageChange(event: PageEvent): void {
    this.filter = { ...this.filter, page: event.pageIndex, size: event.pageSize };
    this.loadInvoices();
  }

  onSortChange(sort: Sort): void {
    this.filter = {
      ...this.filter,
      sort: sort.direction ? sort.active : undefined,
      direction: sort.direction || undefined,
    };
    this.loadInvoices();
  }

  validateInvoice(invoice: IInvoice): void {
    this.invoiceService.validate(invoice.id).subscribe(() => {
      this.notificationService.success(this.translateService.instant('invoices.updatedSuccess'));
      this.loadInvoices();
    });
  }

  confirmDelete(invoice: IInvoice): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: {
        title: this.translateService.instant('invoices.deleteConfirmTitle'),
        message: this.translateService.instant('invoices.deleteConfirmMessage'),
        destructive: true,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteInvoice(invoice);
      }
    });
  }

  exportCsv(): void {
    exportToCsv('factures', this.invoices(), [
      'number',
      'clientName',
      'issueDate',
      'dueDate',
      'totalAmount',
      'status',
    ]);
  }

  exportPdf(): void {
    exportToPdf();
  }

  protected statusTranslationKey(status: InvoiceStatus): string {
    return `invoices.status.${status}`;
  }

  protected statusAccentClass(status: InvoiceStatus): string {
    return this.statusAccent[status];
  }

  private loadInvoices(): void {
    this.isLoading.set(true);
    this.invoiceService.search(this.filter).subscribe({
      next: (page) => {
        this.invoices.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  private deleteInvoice(invoice: IInvoice): void {
    this.invoiceService.delete(invoice.id).subscribe(() => {
      this.notificationService.success(this.translateService.instant('invoices.deletedSuccess'));
      this.loadInvoices();
    });
  }
}
