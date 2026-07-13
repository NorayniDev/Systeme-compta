import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { debounceTime, distinctUntilChanged, finalize, startWith, switchMap } from 'rxjs';
import { QuoteService } from '../../services/quote.service';
import { QuoteStatus } from '../../models/quote.model';
import { QuoteLineDto } from '../../models/quote.dto';
import {
  computeLineItemTotals,
  computeLineTotal,
  ILineItemTotals,
} from '../../../../shared/helpers/line-item-calculation.helper';
import { ClientService } from '../../../clients/services/client.service';
import { IClient } from '../../../clients/models/client.model';
import { NotificationService } from '../../../../core/services/notification.service';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { DEFAULT_PAGE_REQUEST } from '../../../../core/models/pagination.model';

/**
 * Formulaire de création/édition d'un devis: sélection du client par
 * recherche asynchrone, lignes dynamiques (FormArray) et calcul des totaux
 * HT/TVA/TTC en temps réel — même pattern que `InvoiceForm`.
 */
@Component({
  selector: 'app-quote-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    MatProgressSpinnerModule,
    TranslatePipe,
    CurrencyXofPipe,
  ],
  templateUrl: './quote-form.html',
  styleUrl: './quote-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuoteForm implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly quoteService = inject(QuoteService);
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly isLoading = signal(false);
  protected readonly quoteId = signal<string | null>(null);
  protected readonly isEditMode = signal(false);
  protected readonly clientOptions = signal<IClient[]>([]);
  protected readonly totals = signal<ILineItemTotals>({
    amountExclTax: 0,
    taxAmount: 0,
    totalAmount: 0,
  });

  private currentStatus: QuoteStatus = QuoteStatus.DRAFT;

  protected readonly clientSearchControl = new FormControl('', { nonNullable: true });

  protected readonly form = this.formBuilder.nonNullable.group({
    clientId: ['', Validators.required],
    issueDate: [new Date(), Validators.required],
    validUntil: [new Date(Date.now() + 30 * 86_400_000), Validators.required],
    notes: [''],
    lines: this.formBuilder.array([this.createLineGroup()]),
  });

  get lines(): FormArray {
    return this.form.controls.lines;
  }

  ngOnInit(): void {
    this.clientSearchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((query) =>
          this.clientService.search({
            ...DEFAULT_PAGE_REQUEST,
            size: 10,
            query: query || undefined,
          }),
        ),
      )
      .subscribe((page) => this.clientOptions.set(page.content));

    this.form.valueChanges.pipe(startWith(this.form.getRawValue())).subscribe(() => {
      this.totals.set(computeLineItemTotals(this.lines.getRawValue() as QuoteLineDto[]));
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.quoteId.set(id);
      this.isEditMode.set(true);
      this.loadQuote(id);
    }
  }

  addLine(): void {
    this.lines.push(this.createLineGroup());
  }

  removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
    }
  }

  lineTotal(index: number): number {
    const line = this.lines.at(index).getRawValue() as QuoteLineDto;
    return computeLineTotal(line.quantity, line.unitPrice, line.taxRate);
  }

  displayClient(client: IClient | string): string {
    return typeof client === 'string' ? client : (client?.name ?? '');
  }

  onClientSelected(client: IClient): void {
    this.form.controls.clientId.setValue(client.id);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const raw = this.form.getRawValue();
    const dto = {
      clientId: raw.clientId,
      issueDate: this.toIsoDate(raw.issueDate),
      validUntil: this.toIsoDate(raw.validUntil),
      notes: raw.notes,
      lines: raw.lines,
    };
    const id = this.quoteId();

    const request$ = id
      ? this.quoteService.update(id, { ...dto, status: this.currentStatus })
      : this.quoteService.create(dto);

    request$.pipe(finalize(() => this.isSubmitting.set(false))).subscribe(() => {
      const messageKey = id ? 'quotes.updatedSuccess' : 'quotes.createdSuccess';
      this.notificationService.success(this.translateService.instant(messageKey));
      this.router.navigate(['/quotes']);
    });
  }

  private createLineGroup() {
    return this.formBuilder.nonNullable.group({
      description: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]],
      taxRate: [18, [Validators.required, Validators.min(0), Validators.max(100)]],
    });
  }

  private loadQuote(id: string): void {
    this.isLoading.set(true);
    this.quoteService.getById(id).subscribe((quote) => {
      this.currentStatus = quote.status;
      this.form.patchValue({
        clientId: quote.clientId,
        issueDate: new Date(quote.issueDate),
        validUntil: new Date(quote.validUntil),
        notes: quote.notes ?? '',
      });
      this.clientSearchControl.setValue(quote.clientName, { emitEvent: false });

      this.lines.clear();
      for (const line of quote.lines) {
        this.lines.push(
          this.formBuilder.nonNullable.group({
            description: [line.description, Validators.required],
            quantity: [line.quantity, [Validators.required, Validators.min(1)]],
            unitPrice: [line.unitPrice, [Validators.required, Validators.min(0)]],
            taxRate: [line.taxRate, [Validators.required, Validators.min(0), Validators.max(100)]],
          }),
        );
      }
      this.isLoading.set(false);
    });
  }

  private toIsoDate(date: Date): string {
    return new Date(date).toISOString().split('T')[0];
  }
}
