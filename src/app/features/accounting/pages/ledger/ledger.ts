import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe } from '@ngx-translate/core';
import { AccountingService } from '../../services/accounting.service';
import { IJournalEntry } from '../../models/journal-entry.model';
import { IAccount } from '../../models/account.model';
import { DEFAULT_PAGE_REQUEST } from '../../../../core/models/pagination.model';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { formatDate } from '../../../../core/helpers/date.helper';
import { roundToTwoDecimals } from '../../../../core/helpers/currency.helper';

interface ILedgerRow extends IJournalEntry {
  runningBalance: number;
}

/**
 * Grand livre: écritures d'un compte donné, triées chronologiquement, avec
 * solde progressif (débit cumulé - crédit cumulé).
 */
@Component({
  selector: 'app-ledger',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    EmptyState,
    CurrencyXofPipe,
  ],
  templateUrl: './ledger.html',
  styleUrl: './ledger.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Ledger implements OnInit {
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
  protected readonly accounts = signal<IAccount[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly entries = signal<IJournalEntry[]>([]);

  protected readonly accountControl = new FormControl<string | null>(null);

  protected readonly ledgerRows = computed<ILedgerRow[]>(() => {
    let running = 0;
    return this.entries().map((entry) => {
      running = roundToTwoDecimals(running + entry.debit - entry.credit);
      return { ...entry, runningBalance: running };
    });
  });

  ngOnInit(): void {
    this.accountingService.getAccounts().subscribe((accounts) => {
      this.accounts.set(accounts);
      if (accounts.length > 0) {
        this.accountControl.setValue(accounts[0].code);
        this.loadLedger(accounts[0].code);
      }
    });

    this.accountControl.valueChanges.subscribe((code) => {
      if (code) {
        this.loadLedger(code);
      }
    });
  }

  private loadLedger(accountCode: string): void {
    this.isLoading.set(true);
    this.accountingService
      .getLedger(accountCode, { ...DEFAULT_PAGE_REQUEST, size: 100 })
      .subscribe({
        next: (page) => {
          this.entries.set(page.content);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
  }
}
