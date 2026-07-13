import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { ServiceItemService } from '../../services/service-item.service';
import { ServiceItemStatus, ServiceItemUnit } from '../../models/service-item.model';
import { NotificationService } from '../../../../core/services/notification.service';

/**
 * Formulaire de création/édition d'une prestation.
 * Le mode est déterminé par la présence d'un `id` dans la route parente.
 */
@Component({
  selector: 'app-service-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TranslatePipe,
  ],
  templateUrl: './service-form.html',
  styleUrl: './service-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServiceForm implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly serviceItemService = inject(ServiceItemService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly isLoading = signal(false);
  protected readonly serviceItemId = signal<string | null>(null);
  protected readonly isEditMode = signal(false);
  protected readonly units = Object.values(ServiceItemUnit);
  private currentStatus: ServiceItemStatus = ServiceItemStatus.ACTIVE;

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    code: ['', [Validators.required, Validators.maxLength(30)]],
    description: [''],
    category: ['', Validators.required],
    unitPrice: [0, [Validators.required, Validators.min(0)]],
    taxRate: [18, [Validators.required, Validators.min(0), Validators.max(100)]],
    unit: [ServiceItemUnit.HOUR, Validators.required],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.serviceItemId.set(id);
      this.isEditMode.set(true);
      this.loadServiceItem(id);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const dto = this.form.getRawValue();
    const id = this.serviceItemId();

    const request$ = id
      ? this.serviceItemService.update(id, { ...dto, status: this.currentStatus })
      : this.serviceItemService.create(dto);

    request$.pipe(finalize(() => this.isSubmitting.set(false))).subscribe(() => {
      const messageKey = id ? 'services.updatedSuccess' : 'services.createdSuccess';
      this.notificationService.success(this.translateService.instant(messageKey));
      this.router.navigate(['/services']);
    });
  }

  private loadServiceItem(id: string): void {
    this.isLoading.set(true);
    this.serviceItemService.getById(id).subscribe((serviceItem) => {
      this.currentStatus = serviceItem.status;
      this.form.patchValue(serviceItem);
      this.isLoading.set(false);
    });
  }
}
