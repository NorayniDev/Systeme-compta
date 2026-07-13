import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe } from '@ngx-translate/core';
import { debounceTime } from 'rxjs';
import { AccountingService, IJournalFilter } from '../../services/accounting.service';
import { IJournalEntry } from '../../models/journal-entry.model';
import { DEFAULT_PAGE_REQUEST } from '../../../../core/models/pagination.model';
import { APP_CONSTANTS } from '../../../../core/constants/app.constant';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { exportToCsv, exportToPdf } from '../../../../shared/helpers/table-export.helper';
import { formatDate } from '../../../../core/helpers/date.helper';

/**
 * Journal comptable: liste chronologique de toutes les écritures (débit et
 * crédit), filtrable par période. Vue en lecture seule — les écritures sont
 * générées côté backend à partir des opérations métier.
 */
@Component({
  selector: 'app-journal',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDatepickerModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    EmptyState,
    CurrencyXofPipe,
  ],
  templateUrl: './journal.html',
  styleUrl: './journal.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Journal implements OnInit {
  private readonly accountingService = inject(AccountingService);
  private readonly formBuilder = inject(FormBuilder);
  protected readonly formatDate = formatDate;

  protected readonly displayedColumns = [
    'date',
    'reference',
    'accountCode',
    'label',
    'debit',
    'credit',
  ];
  protected readonly pageSizeOptions = APP_CONSTANTS.PAGE_SIZE_OPTIONS;

  protected readonly periodForm = this.formBuilder.nonNullable.group({
    startDate: [null as Date | null],
    endDate: [null as Date | null],
  });

  protected readonly isLoading = signal(true);
  protected readonly entries = signal<IJournalEntry[]>([]);
  protected readonly totalElements = signal(0);

  private filter: IJournalFilter = { ...DEFAULT_PAGE_REQUEST, size: this.pageSizeOptions[0] };

  ngOnInit(): void {
    this.loadJournal();

    this.periodForm.valueChanges.pipe(debounceTime(300)).subscribe((period) => {
      this.filter = {
        ...this.filter,
        page: 0,
        startDate: period.startDate ? this.toIsoDate(period.startDate) : undefined,
        endDate: period.endDate ? this.toIsoDate(period.endDate) : undefined,
      };
      this.loadJournal();
    });
  }

  onPageChange(event: PageEvent): void {
    this.filter = { ...this.filter, page: event.pageIndex, size: event.pageSize };
    this.loadJournal();
  }

  exportCsv(): void {
    exportToCsv('journal-comptable', this.entries(), [
      'date',
      'reference',
      'accountCode',
      'accountName',
      'label',
      'debit',
      'credit',
    ]);
  }

  exportPdf(): void {
    exportToPdf();
  }

  private loadJournal(): void {
    this.isLoading.set(true);
    this.accountingService.getJournal(this.filter).subscribe({
      next: (page) => {
        this.entries.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  private toIsoDate(date: Date): string {
    return new Date(date).toISOString().split('T')[0];
  }
}
