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
import { QuoteService } from '../../services/quote.service';
import { IQuote, QUOTE_STATUS_ACCENT, QuoteStatus } from '../../models/quote.model';
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
 * Liste des devis: recherche, tri, pagination côté serveur, export,
 * conversion rapide en facture et suppression avec confirmation.
 */
@Component({
  selector: 'app-quote-list',
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
  templateUrl: './quote-list.html',
  styleUrl: './quote-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuoteList implements OnInit {
  private readonly quoteService = inject(QuoteService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly dialog = inject(MatDialog);

  protected readonly Permission = Permission;
  protected readonly statusAccent = QUOTE_STATUS_ACCENT;
  protected readonly formatDate = formatDate;
  protected readonly displayedColumns = [
    'number',
    'clientName',
    'issueDate',
    'validUntil',
    'totalAmount',
    'status',
    'actions',
  ];
  protected readonly pageSizeOptions = APP_CONSTANTS.PAGE_SIZE_OPTIONS;

  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly isLoading = signal(true);
  protected readonly quotes = signal<IQuote[]>([]);
  protected readonly totalElements = signal(0);

  private filter: ISearchFilter = { ...DEFAULT_PAGE_REQUEST, size: this.pageSizeOptions[0] };

  ngOnInit(): void {
    this.loadQuotes();

    this.searchControl.valueChanges
      .pipe(debounceTime(APP_CONSTANTS.DEBOUNCE_SEARCH_MS), distinctUntilChanged())
      .subscribe((query) => {
        this.filter = { ...this.filter, query, page: 0 };
        this.loadQuotes();
      });
  }

  onPageChange(event: PageEvent): void {
    this.filter = { ...this.filter, page: event.pageIndex, size: event.pageSize };
    this.loadQuotes();
  }

  onSortChange(sort: Sort): void {
    this.filter = {
      ...this.filter,
      sort: sort.direction ? sort.active : undefined,
      direction: sort.direction || undefined,
    };
    this.loadQuotes();
  }

  convertToInvoice(quote: IQuote): void {
    this.quoteService.convertToInvoice(quote.id).subscribe(() => {
      this.notificationService.success(this.translateService.instant('quotes.convertedSuccess'));
      this.loadQuotes();
    });
  }

  confirmDelete(quote: IQuote): void {
    const dialogRef = this.dialog.open(ConfirmDialog, {
      data: {
        title: this.translateService.instant('quotes.deleteConfirmTitle'),
        message: this.translateService.instant('quotes.deleteConfirmMessage'),
        destructive: true,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteQuote(quote);
      }
    });
  }

  exportCsv(): void {
    exportToCsv('devis', this.quotes(), [
      'number',
      'clientName',
      'issueDate',
      'validUntil',
      'totalAmount',
      'status',
    ]);
  }

  exportPdf(): void {
    exportToPdf();
  }

  protected statusTranslationKey(status: QuoteStatus): string {
    return `quotes.status.${status}`;
  }

  protected statusAccentClass(status: QuoteStatus): string {
    return this.statusAccent[status];
  }

  protected canConvert(status: QuoteStatus): boolean {
    return status === QuoteStatus.ACCEPTED;
  }

  private loadQuotes(): void {
    this.isLoading.set(true);
    this.quoteService.search(this.filter).subscribe({
      next: (page) => {
        this.quotes.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  private deleteQuote(quote: IQuote): void {
    this.quoteService.delete(quote.id).subscribe(() => {
      this.notificationService.success(this.translateService.instant('quotes.deletedSuccess'));
      this.loadQuotes();
    });
  }
}
