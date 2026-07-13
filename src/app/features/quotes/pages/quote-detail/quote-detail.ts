import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { QuoteService } from '../../services/quote.service';
import { IQuote, QUOTE_STATUS_ACCENT, QuoteStatus } from '../../models/quote.model';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';
import { formatDate } from '../../../../core/helpers/date.helper';
import { NotificationService } from '../../../../core/services/notification.service';
import { downloadBlob } from '../../../../shared/helpers/table-export.helper';

/** Fiche de détail d'un devis: en-tête, lignes, totaux et actions de cycle de vie. */
@Component({
  selector: 'app-quote-detail',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTableModule,
    TranslatePipe,
    CurrencyXofPipe,
    HasPermissionDirective,
  ],
  templateUrl: './quote-detail.html',
  styleUrl: './quote-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuoteDetail implements OnInit {
  private readonly quoteService = inject(QuoteService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly Permission = Permission;
  protected readonly QuoteStatus = QuoteStatus;
  protected readonly statusAccent = QUOTE_STATUS_ACCENT;
  protected readonly formatDate = formatDate;
  protected readonly lineColumns = ['description', 'quantity', 'unitPrice', 'taxRate', 'lineTotal'];
  protected readonly isLoading = signal(true);
  protected readonly isDownloading = signal(false);
  protected readonly quote = signal<IQuote | null>(null);

  ngOnInit(): void {
    this.loadQuote();
  }

  downloadPdf(): void {
    const current = this.quote();
    if (!current) return;

    this.isDownloading.set(true);
    this.quoteService.downloadPdf(current.id).subscribe({
      next: (blob) => {
        downloadBlob(blob, `${current.number}.pdf`);
        this.isDownloading.set(false);
      },
      error: () => {
        this.notificationService.error(this.translateService.instant('common.downloadError'));
        this.isDownloading.set(false);
      },
    });
  }

  send(): void {
    this.runAction((id) => this.quoteService.send(id), 'quotes.updatedSuccess');
  }

  accept(): void {
    this.runAction((id) => this.quoteService.accept(id), 'quotes.updatedSuccess');
  }

  reject(): void {
    this.runAction((id) => this.quoteService.reject(id), 'quotes.updatedSuccess');
  }

  convertToInvoice(): void {
    const current = this.quote();
    if (!current) return;

    this.quoteService.convertToInvoice(current.id).subscribe((invoice) => {
      this.notificationService.success(this.translateService.instant('quotes.convertedSuccess'));
      this.router.navigate(['/invoices', invoice.id]);
    });
  }

  private runAction(action: (id: string) => Observable<IQuote>, messageKey: string): void {
    const current = this.quote();
    if (!current) return;

    action(current.id).subscribe((updated) => {
      this.quote.set(updated);
      this.notificationService.success(this.translateService.instant(messageKey));
    });
  }

  private loadQuote(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.quoteService.getById(id).subscribe((quote) => {
      this.quote.set(quote);
      this.isLoading.set(false);
    });
  }
}
