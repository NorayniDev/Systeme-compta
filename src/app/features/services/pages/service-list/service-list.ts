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
import { ServiceItemService } from '../../services/service-item.service';
import { IServiceItem, ServiceItemStatus } from '../../models/service-item.model';
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
 * Liste des prestations: recherche, tri, pagination côté serveur, export et
 * suppression avec confirmation. Symétrique de `ProductList`, sans stock.
 */
@Component({
  selector: 'app-service-list',
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
  templateUrl: './service-list.html',
  styleUrl: './service-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServiceList implements OnInit {
  private readonly serviceItemService = inject(ServiceItemService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly dialog = inject(MatDialog);

  protected readonly Permission = Permission;
  protected readonly displayedColumns = [
    'name',
    'code',
    'category',
    'unitPrice',
    'unit',
    'status',
    'actions',
  ];
  protected readonly pageSizeOptions = APP_CONSTANTS.PAGE_SIZE_OPTIONS;

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly isLoading = signal(true);
  protected readonly serviceItems = signal<IServiceItem[]>([]);
  protected readonly totalElements = signal(0);

  private filter: ISearchFilter = { ...DEFAULT_PAGE_REQUEST, size: this.pageSizeOptions[0] };

  ngOnInit(): void {
    this.loadServiceItems();

    this.searchControl.valueChanges
      .pipe(debounceTime(APP_CONSTANTS.DEBOUNCE_SEARCH_MS), distinctUntilChanged())
      .subscribe((query) => {
        this.filter = { ...this.filter, query, page: 0 };
        this.loadServiceItems();
      });
  }

  onPageChange(event: PageEvent): void {
    this.filter = { ...this.filter, page: event.pageIndex, size: event.pageSize };
    this.loadServiceItems();
  }

  onSortChange(sort: Sort): void {
    this.filter = {
      ...this.filter,
      sort: sort.direction ? sort.active : undefined,
      direction: sort.direction || undefined,
    };
    this.loadServiceItems();
  }

  confirmDelete(serviceItem: IServiceItem): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: {
        title: this.translateService.instant('services.deleteConfirmTitle'),
        message: this.translateService.instant('services.deleteConfirmMessage'),
        destructive: true,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteServiceItem(serviceItem);
      }
    });
  }

  exportCsv(): void {
    exportToCsv('services', this.serviceItems(), [
      'name',
      'code',
      'category',
      'unitPrice',
      'unit',
      'status',
    ]);
  }

  exportPdf(): void {
    exportToPdf();
  }

  protected statusLabel(status: ServiceItemStatus): string {
    return status === ServiceItemStatus.ACTIVE ? 'common.active' : 'common.inactive';
  }

  protected unitTranslationKey(unit: IServiceItem['unit']): string {
    return `services.unit.${unit}`;
  }

  private loadServiceItems(): void {
    this.isLoading.set(true);
    this.serviceItemService.search(this.filter).subscribe({
      next: (page) => {
        this.serviceItems.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  private deleteServiceItem(serviceItem: IServiceItem): void {
    this.serviceItemService.delete(serviceItem.id).subscribe(() => {
      this.notificationService.success(this.translateService.instant('services.deletedSuccess'));
      this.loadServiceItems();
    });
  }
}
