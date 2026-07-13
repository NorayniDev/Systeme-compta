import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { debounceTime, distinctUntilChanged, finalize, switchMap } from 'rxjs';
import { PaymentService } from '../../services/payment.service';
import { PaymentMethod } from '../../models/payment.model';
import { InvoiceService } from '../../../invoices/services/invoice.service';
import { IInvoice } from '../../../invoices/models/invoice.model';
import { NotificationService } from '../../../../core/services/notification.service';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { DEFAULT_PAGE_REQUEST } from '../../../../core/models/pagination.model';

/**
 * Formulaire d'enregistrement d'un encaissement: sélection de la facture par
 * recherche asynchrone (numéro ou client), montant, moyen de paiement et date.
 * Un paiement, une fois enregistré, n'est pas modifiable — seule l'action
 * de remboursement (`PaymentDetail`) permet de revenir dessus.
 */
@Component({
  selector: 'app-payment-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    CurrencyXofPipe,
  ],
  templateUrl: './payment-form.html',
  styleUrl: './payment-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentForm implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly paymentService = inject(PaymentService);
  private readonly invoiceService = inject(InvoiceService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly invoiceOptions = signal<IInvoice[]>([]);
  protected readonly selectedInvoice = signal<IInvoice | null>(null);
  protected readonly paymentMethods = Object.values(PaymentMethod);

  protected readonly invoiceSearchControl = new FormControl('', { nonNullable: true });

  protected readonly form = this.formBuilder.nonNullable.group({
    invoiceId: ['', Validators.required],
    amount: [0, [Validators.required, Validators.min(1)]],
    method: [PaymentMethod.BANK_TRANSFER, Validators.required],
    paidAt: [new Date(), Validators.required],
    notes: [''],
  });

  ngOnInit(): void {
    this.invoiceSearchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((query) =>
          this.invoiceService.search({
            ...DEFAULT_PAGE_REQUEST,
            size: 10,
            query: query || undefined,
          }),
        ),
      )
      .subscribe((page) => this.invoiceOptions.set(page.content));

    const invoiceId = this.route.snapshot.queryParamMap.get('invoiceId');
    if (invoiceId) {
      this.invoiceService.getById(invoiceId).subscribe((invoice) => this.selectInvoice(invoice));
    }
  }

  displayInvoice(invoice: IInvoice | string): string {
    return typeof invoice === 'string'
      ? invoice
      : invoice
        ? `${invoice.number} — ${invoice.clientName}`
        : '';
  }

  onInvoiceSelected(invoice: IInvoice): void {
    this.selectInvoice(invoice);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const raw = this.form.getRawValue();
    const dto = { ...raw, paidAt: this.toIsoDate(raw.paidAt) };

    this.paymentService
      .create(dto)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe(() => {
        this.notificationService.success(this.translateService.instant('payments.createdSuccess'));
        this.router.navigate(['/payments']);
      });
  }

  private selectInvoice(invoice: IInvoice): void {
    this.selectedInvoice.set(invoice);
    this.invoiceSearchControl.setValue(this.displayInvoice(invoice), { emitEvent: false });
    this.form.patchValue({ invoiceId: invoice.id, amount: invoice.totalAmount });
  }

  private toIsoDate(date: Date): string {
    return new Date(date).toISOString().split('T')[0];
  }
}
