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
import { SupplierService } from '../../services/supplier.service';
import { ISupplier, SupplierStatus } from '../../models/supplier.model';
import { DEFAULT_PAGE_REQUEST, ISearchFilter } from '../../../../core/models/pagination.model';
import { APP_CONSTANTS } from '../../../../core/constants/app.constant';
import { NotificationService } from '../../../../core/services/notification.service';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';
import { exportToCsv, exportToPdf } from '../../../../shared/helpers/table-export.helper';

/**
 * Liste des fournisseurs: recherche, tri, pagination côté serveur, export et
 * suppression avec confirmation. Symétrique de `ClientList`.
 */
@Component({
  selector: 'app-supplier-list',
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
  templateUrl: './supplier-list.html',
  styleUrl: './supplier-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SupplierList implements OnInit {
  private readonly supplierService = inject(SupplierService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly dialog = inject(MatDialog);

  protected readonly Permission = Permission;
  protected readonly displayedColumns = [
    'name',
    'email',
    'phone',
    'totalPurchased',
    'status',
    'actions',
  ];
  protected readonly pageSizeOptions = APP_CONSTANTS.PAGE_SIZE_OPTIONS;

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly isLoading = signal(true);
  protected readonly suppliers = signal<ISupplier[]>([]);
  protected readonly totalElements = signal(0);

  private filter: ISearchFilter = { ...DEFAULT_PAGE_REQUEST, size: this.pageSizeOptions[0] };

  ngOnInit(): void {
    this.loadSuppliers();

    this.searchControl.valueChanges
      .pipe(debounceTime(APP_CONSTANTS.DEBOUNCE_SEARCH_MS), distinctUntilChanged())
      .subscribe((query) => {
        this.filter = { ...this.filter, query, page: 0 };
        this.loadSuppliers();
      });
  }

  onPageChange(event: PageEvent): void {
    this.filter = { ...this.filter, page: event.pageIndex, size: event.pageSize };
    this.loadSuppliers();
  }

  onSortChange(sort: Sort): void {
    this.filter = {
      ...this.filter,
      sort: sort.direction ? sort.active : undefined,
      direction: sort.direction || undefined,
    };
    this.loadSuppliers();
  }

  confirmDelete(supplier: ISupplier): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: {
        title: this.translateService.instant('suppliers.deleteConfirmTitle'),
        message: this.translateService.instant('suppliers.deleteConfirmMessage'),
        destructive: true,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteSupplier(supplier);
      }
    });
  }

  exportCsv(): void {
    exportToCsv('fournisseurs', this.suppliers(), [
      'name',
      'email',
      'phone',
      'address',
      'taxId',
      'status',
    ]);
  }

  exportPdf(): void {
    exportToPdf();
  }

  protected statusLabel(status: SupplierStatus): string {
    return status === SupplierStatus.ACTIVE ? 'common.active' : 'common.inactive';
  }

  private loadSuppliers(): void {
    this.isLoading.set(true);
    this.supplierService.search(this.filter).subscribe({
      next: (page) => {
        this.suppliers.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  private deleteSupplier(supplier: ISupplier): void {
    this.supplierService.delete(supplier.id).subscribe(() => {
      this.notificationService.success(this.translateService.instant('suppliers.deletedSuccess'));
      this.loadSuppliers();
    });
  }
}
