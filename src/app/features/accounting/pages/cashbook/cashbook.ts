import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';
import { AccountingService } from '../../services/accounting.service';
import { IJournalEntry } from '../../models/journal-entry.model';
import { DEFAULT_PAGE_REQUEST } from '../../../../core/models/pagination.model';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { formatDate } from '../../../../core/helpers/date.helper';
import { roundToTwoDecimals } from '../../../../core/helpers/currency.helper';
import { exportToCsv, exportToPdf } from '../../../../shared/helpers/table-export.helper';

interface ICashbookRow extends IJournalEntry {
  runningBalance: number;
}

/**
 * Journal de caisse: mouvements des comptes de trésorerie (caisse/banque),
 * avec solde progressif — vue restreinte du journal général.
 */
@Component({
  selector: 'app-cashbook',
  standalone: true,
  imports: [
    MatTableModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatMenuModule,
    MatIconModule,
    TranslatePipe,
    EmptyState,
    CurrencyXofPipe,
  ],
  templateUrl: './cashbook.html',
  styleUrl: './cashbook.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Cashbook implements OnInit {
  private readonly accountingService = inject(AccountingService);
  protected readonly formatDate = formatDate;

  protected readonly displayedColumns = [
    'date',
    'reference',
    'label',
    'debit',
    'credit',
    'runningBalance',
  ];
  protected readonly isLoading = signal(true);
  protected readonly entries = signal<IJournalEntry[]>([]);

  protected readonly rows = computed<ICashbookRow[]>(() => {
    let running = 0;
    return this.entries().map((entry) => {
      running = roundToTwoDecimals(running + entry.debit - entry.credit);
      return { ...entry, runningBalance: running };
    });
  });

  ngOnInit(): void {
    this.accountingService.getCashbook({ ...DEFAULT_PAGE_REQUEST, size: 100 }).subscribe({
      next: (page) => {
        this.entries.set(page.content);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  exportCsv(): void {
    exportToCsv('journal-de-caisse', this.entries(), [
      'date',
      'reference',
      'label',
      'debit',
      'credit',
    ]);
  }

  exportPdf(): void {
    exportToPdf();
  }
}
