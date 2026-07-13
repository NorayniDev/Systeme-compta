import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe } from '@ngx-translate/core';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AuditService } from '../../services/audit.service';
import { AuditAction, IAuditLog } from '../../models/audit-log.model';
import { DEFAULT_PAGE_REQUEST, ISearchFilter } from '../../../../core/models/pagination.model';
import { APP_CONSTANTS } from '../../../../core/constants/app.constant';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { Permission } from '../../../../core/constants/roles.constant';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { exportToCsv, exportToPdf } from '../../../../shared/helpers/table-export.helper';
import { formatDateTime } from '../../../../core/helpers/date.helper';

/**
 * Journal d'audit: consultation seule des actions sensibles tracées par le
 * backend (création, modification, suppression, connexion, validation,
 * export). Aucune action de modification n'est exposée ici par conception.
 */
@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [
    ReactiveFormsModule,
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
    HasPermissionDirective,
  ],
  templateUrl: './audit-log.html',
  styleUrl: './audit-log.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuditLog implements OnInit {
  private readonly auditService = inject(AuditService);

  protected readonly Permission = Permission;
  protected readonly displayedColumns = ['timestamp', 'userName', 'action', 'entity', 'details'];
  protected readonly pageSizeOptions = APP_CONSTANTS.PAGE_SIZE_OPTIONS;

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly isLoading = signal(true);
  protected readonly logs = signal<IAuditLog[]>([]);
  protected readonly totalElements = signal(0);

  private filter: ISearchFilter = { ...DEFAULT_PAGE_REQUEST, size: this.pageSizeOptions[0] };

  ngOnInit(): void {
    this.loadLogs();

    this.searchControl.valueChanges
      .pipe(debounceTime(APP_CONSTANTS.DEBOUNCE_SEARCH_MS), distinctUntilChanged())
      .subscribe((query) => {
        this.filter = { ...this.filter, query, page: 0 };
        this.loadLogs();
      });
  }

  onPageChange(event: PageEvent): void {
    this.filter = { ...this.filter, page: event.pageIndex, size: event.pageSize };
    this.loadLogs();
  }

  onSortChange(sort: Sort): void {
    this.filter = {
      ...this.filter,
      sort: sort.direction ? sort.active : undefined,
      direction: sort.direction || undefined,
    };
    this.loadLogs();
  }

  exportCsv(): void {
    exportToCsv('audit-logs', this.logs(), [
      'timestamp',
      'userName',
      'action',
      'entityType',
      'entityLabel',
      'details',
    ]);
  }

  exportPdf(): void {
    exportToPdf();
  }

  protected formatTimestamp(timestamp: string): string {
    return formatDateTime(timestamp);
  }

  protected actionIcon(action: AuditAction): string {
    switch (action) {
      case AuditAction.CREATE:
        return 'add_circle';
      case AuditAction.UPDATE:
        return 'edit';
      case AuditAction.DELETE:
        return 'delete';
      case AuditAction.LOGIN:
        return 'login';
      case AuditAction.VALIDATE:
        return 'check_circle';
      case AuditAction.EXPORT:
        return 'download';
      default:
        return 'info';
    }
  }

  private loadLogs(): void {
    this.isLoading.set(true);
    this.auditService.getLogs(this.filter).subscribe({
      next: (page) => {
        this.logs.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }
}
