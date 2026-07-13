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
import { ITrialBalanceLine } from '../../models/trial-balance.model';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { roundToTwoDecimals } from '../../../../core/helpers/currency.helper';
import { exportToCsv, exportToPdf } from '../../../../shared/helpers/table-export.helper';

/**
 * Balance générale: cumul débit/crédit et solde de chaque compte sur la
 * période. La ligne de total permet de vérifier l'équilibre de la partie
 * double (total débit = total crédit).
 */
@Component({
  selector: 'app-trial-balance',
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
  templateUrl: './trial-balance.html',
  styleUrl: './trial-balance.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrialBalance implements OnInit {
  private readonly accountingService = inject(AccountingService);

  protected readonly displayedColumns = [
    'accountCode',
    'accountName',
    'totalDebit',
    'totalCredit',
    'balance',
  ];
  protected readonly isLoading = signal(true);
  protected readonly lines = signal<ITrialBalanceLine[]>([]);

  protected readonly totals = computed(() => {
    const lines = this.lines();
    return {
      totalDebit: roundToTwoDecimals(lines.reduce((sum, line) => sum + line.totalDebit, 0)),
      totalCredit: roundToTwoDecimals(lines.reduce((sum, line) => sum + line.totalCredit, 0)),
    };
  });

  ngOnInit(): void {
    this.accountingService.getTrialBalance({}).subscribe({
      next: (lines) => {
        this.lines.set(lines);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  exportCsv(): void {
    exportToCsv('balance-generale', this.lines(), [
      'accountCode',
      'accountName',
      'totalDebit',
      'totalCredit',
      'balance',
    ]);
  }

  exportPdf(): void {
    exportToPdf();
  }
}
