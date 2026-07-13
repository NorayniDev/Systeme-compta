import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { SettingsService } from '../../services/settings.service';
import { NotificationService } from '../../../../core/services/notification.service';

/**
 * Paramètres généraux de l'entreprise (raison sociale, coordonnées, TVA par
 * défaut, préfixes de numérotation). Réservé aux titulaires de
 * `SETTINGS_MANAGE` — voir `settings.routes.ts`.
 */
@Component({
  selector: 'app-company-settings',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    TranslatePipe,
  ],
  templateUrl: './company-settings.html',
  styleUrl: './company-settings.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CompanySettings implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly settingsService = inject(SettingsService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);

  protected readonly isLoading = signal(true);
  protected readonly isSubmitting = signal(false);

  protected readonly form = this.formBuilder.nonNullable.group({
    companyName: ['', Validators.required],
    address: ['', Validators.required],
    taxId: ['', Validators.required],
    currency: ['XOF', Validators.required],
    defaultTaxRate: [18, [Validators.required, Validators.min(0), Validators.max(100)]],
    invoicePrefix: ['FAC', Validators.required],
    quotePrefix: ['DEV', Validators.required],
  });

  ngOnInit(): void {
    this.settingsService.getCompanySettings().subscribe((settings) => {
      this.form.patchValue(settings);
      this.isLoading.set(false);
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.settingsService
      .updateCompanySettings(this.form.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe(() => {
        this.notificationService.success(
          this.translateService.instant('settings.company.updatedSuccess'),
        );
      });
  }
}
