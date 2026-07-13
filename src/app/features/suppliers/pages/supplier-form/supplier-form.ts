import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { SupplierService } from '../../services/supplier.service';
import {
  phoneNumberValidator,
  taxIdValidator,
} from '../../../../shared/validators/contact.validators';
import { SupplierStatus } from '../../models/supplier.model';
import { NotificationService } from '../../../../core/services/notification.service';

/**
 * Formulaire de création/édition d'un fournisseur.
 * Le mode est déterminé par la présence d'un `id` dans la route parente.
 */
@Component({
  selector: 'app-supplier-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TranslatePipe,
  ],
  templateUrl: './supplier-form.html',
  styleUrl: './supplier-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SupplierForm implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly supplierService = inject(SupplierService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly isLoading = signal(false);
  protected readonly supplierId = signal<string | null>(null);
  protected readonly isEditMode = signal(false);
  private currentStatus: SupplierStatus = SupplierStatus.ACTIVE;

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, phoneNumberValidator()]],
    address: ['', [Validators.required]],
    taxId: ['', [Validators.required, taxIdValidator()]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.supplierId.set(id);
      this.isEditMode.set(true);
      this.loadSupplier(id);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const dto = this.form.getRawValue();
    const id = this.supplierId();

    const request$ = id
      ? this.supplierService.update(id, { ...dto, status: this.currentStatus })
      : this.supplierService.create(dto);

    request$.pipe(finalize(() => this.isSubmitting.set(false))).subscribe(() => {
      const messageKey = id ? 'suppliers.updatedSuccess' : 'suppliers.createdSuccess';
      this.notificationService.success(this.translateService.instant(messageKey));
      this.router.navigate(['/suppliers']);
    });
  }

  private loadSupplier(id: string): void {
    this.isLoading.set(true);
    this.supplierService.getById(id).subscribe((supplier) => {
      this.currentStatus = supplier.status;
      this.form.patchValue(supplier);
      this.isLoading.set(false);
    });
  }
}
