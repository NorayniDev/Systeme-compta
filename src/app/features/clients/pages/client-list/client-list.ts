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
import { ClientService } from '../../services/client.service';
import { ClientStatus, IClient } from '../../models/client.model';
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
 * Liste des clients: recherche, tri, pagination côté serveur, export et
 * suppression avec confirmation. Toute la logique d'accès aux données passe
 * par `ClientService` — ce composant orchestre uniquement l'UI.
 */
@Component({
  selector: 'app-client-list',
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
  templateUrl: './client-list.html',
  styleUrl: './client-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientList implements OnInit {
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly dialog = inject(MatDialog);

  protected readonly Permission = Permission;
  protected readonly displayedColumns = [
    'name',
    'email',
    'phone',
    'totalInvoiced',
    'status',
    'actions',
  ];
  protected readonly pageSizeOptions = APP_CONSTANTS.PAGE_SIZE_OPTIONS;

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly isLoading = signal(true);
  protected readonly clients = signal<IClient[]>([]);
  protected readonly totalElements = signal(0);

  private filter: ISearchFilter = { ...DEFAULT_PAGE_REQUEST, size: this.pageSizeOptions[0] };

  ngOnInit(): void {
    this.loadClients();

    this.searchControl.valueChanges
      .pipe(debounceTime(APP_CONSTANTS.DEBOUNCE_SEARCH_MS), distinctUntilChanged())
      .subscribe((query) => {
        this.filter = { ...this.filter, query, page: 0 };
        this.loadClients();
      });
  }

  onPageChange(event: PageEvent): void {
    this.filter = { ...this.filter, page: event.pageIndex, size: event.pageSize };
    this.loadClients();
  }

  onSortChange(sort: Sort): void {
    this.filter = {
      ...this.filter,
      sort: sort.direction ? sort.active : undefined,
      direction: sort.direction || undefined,
    };
    this.loadClients();
  }

  confirmDelete(client: IClient): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: {
        title: this.translateService.instant('clients.deleteConfirmTitle'),
        message: this.translateService.instant('clients.deleteConfirmMessage'),
        destructive: true,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteClient(client);
      }
    });
  }

  exportCsv(): void {
    exportToCsv('clients', this.clients(), [
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

  protected statusLabel(status: ClientStatus): string {
    return status === ClientStatus.ACTIVE ? 'common.active' : 'common.inactive';
  }

  private loadClients(): void {
    this.isLoading.set(true);
    this.clientService.search(this.filter).subscribe({
      next: (page) => {
        this.clients.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  private deleteClient(client: IClient): void {
    this.clientService.delete(client.id).subscribe(() => {
      this.notificationService.success(this.translateService.instant('clients.deletedSuccess'));
      this.loadClients();
    });
  }
}
