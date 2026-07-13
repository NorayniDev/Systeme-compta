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
import { PaymentService } from '../../services/payment.service';
import { IPayment, PAYMENT_STATUS_ACCENT, PaymentStatus } from '../../models/payment.model';
import { DEFAULT_PAGE_REQUEST, ISearchFilter } from '../../../../core/models/pagination.model';
import { APP_CONSTANTS } from '../../../../core/constants/app.constant';
import { NotificationService } from '../../../../core/services/notification.service';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';
import { exportToCsv, exportToPdf } from '../../../../shared/helpers/table-export.helper';
import { formatDate } from '../../../../core/helpers/date.helper';

/**
 * Liste des paiements: recherche, tri, pagination côté serveur, export,
 * remboursement rapide et suppression avec confirmation.
 */
@Component({
  selector: 'app-payment-list',
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
  templateUrl: './payment-list.html',
  styleUrl: './payment-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentList implements OnInit {
  private readonly paymentService = inject(PaymentService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly dialog = inject(MatDialog);

  protected readonly Permission = Permission;
  protected readonly statusAccent = PAYMENT_STATUS_ACCENT;
  protected readonly formatDate = formatDate;
  protected readonly displayedColumns = [
    'reference',
    'invoiceNumber',
    'clientName',
    'paidAt',
    'amount',
    'method',
    'status',
    'actions',
  ];
  protected readonly pageSizeOptions = APP_CONSTANTS.PAGE_SIZE_OPTIONS;

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly isLoading = signal(true);
  protected readonly payments = signal<IPayment[]>([]);
  protected readonly totalElements = signal(0);

  private filter: ISearchFilter = { ...DEFAULT_PAGE_REQUEST, size: this.pageSizeOptions[0] };

  ngOnInit(): void {
    this.loadPayments();

    this.searchControl.valueChanges
      .pipe(debounceTime(APP_CONSTANTS.DEBOUNCE_SEARCH_MS), distinctUntilChanged())
      .subscribe((query) => {
        this.filter = { ...this.filter, query, page: 0 };
        this.loadPayments();
      });
  }

  onPageChange(event: PageEvent): void {
    this.filter = { ...this.filter, page: event.pageIndex, size: event.pageSize };
    this.loadPayments();
  }

  onSortChange(sort: Sort): void {
    this.filter = {
      ...this.filter,
      sort: sort.direction ? sort.active : undefined,
      direction: sort.direction || undefined,
    };
    this.loadPayments();
  }

  confirmRefund(payment: IPayment): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: {
        title: this.translateService.instant('payments.refundConfirmTitle'),
        message: this.translateService.instant('payments.refundConfirmMessage'),
        destructive: true,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.refundPayment(payment);
      }
    });
  }

  confirmDelete(payment: IPayment): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: {
        title: this.translateService.instant('payments.deleteConfirmTitle'),
        message: this.translateService.instant('payments.deleteConfirmMessage'),
        destructive: true,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deletePayment(payment);
      }
    });
  }

  exportCsv(): void {
    exportToCsv('paiements', this.payments(), [
      'reference',
      'invoiceNumber',
      'clientName',
      'paidAt',
      'amount',
      'method',
      'status',
    ]);
  }

  exportPdf(): void {
    exportToPdf();
  }

  protected statusTranslationKey(status: PaymentStatus): string {
    return `payments.status.${status}`;
  }

  protected methodTranslationKey(method: IPayment['method']): string {
    return `payments.method.${method}`;
  }

  protected statusAccentClass(status: PaymentStatus): string {
    return this.statusAccent[status];
  }

  protected canRefund(status: PaymentStatus): boolean {
    return status === PaymentStatus.COMPLETED;
  }

  private loadPayments(): void {
    this.isLoading.set(true);
    this.paymentService.search(this.filter).subscribe({
      next: (page) => {
        this.payments.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  private refundPayment(payment: IPayment): void {
    this.paymentService.refund(payment.id).subscribe(() => {
      this.notificationService.success(this.translateService.instant('payments.refundedSuccess'));
      this.loadPayments();
    });
  }

  private deletePayment(payment: IPayment): void {
    this.paymentService.delete(payment.id).subscribe(() => {
      this.notificationService.success(this.translateService.instant('payments.deletedSuccess'));
      this.loadPayments();
    });
  }
}
