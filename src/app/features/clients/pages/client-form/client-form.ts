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
import { ClientService } from '../../services/client.service';
import {
  phoneNumberValidator,
  taxIdValidator,
} from '../../../../shared/validators/contact.validators';
import { ClientStatus } from '../../models/client.model';
import { NotificationService } from '../../../../core/services/notification.service';

/**
 * Formulaire de création/édition d'un client.
 * Le mode est déterminé par la présence d'un `id` dans la route parente.
 */
@Component({
  selector: 'app-client-form',
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
  templateUrl: './client-form.html',
  styleUrl: './client-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientForm implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly clientService = inject(ClientService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly isLoading = signal(false);
  protected readonly clientId = signal<string | null>(null);
  protected readonly isEditMode = signal(false);
  private currentStatus: ClientStatus = ClientStatus.ACTIVE;

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
      this.clientId.set(id);
      this.isEditMode.set(true);
      this.loadClient(id);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const dto = this.form.getRawValue();
    const id = this.clientId();

    const request$ = id
      ? this.clientService.update(id, { ...dto, status: this.currentStatus })
      : this.clientService.create(dto);

    request$.pipe(finalize(() => this.isSubmitting.set(false))).subscribe(() => {
      const messageKey = id ? 'clients.updatedSuccess' : 'clients.createdSuccess';
      this.notificationService.success(this.translateService.instant(messageKey));
      this.router.navigate(['/clients']);
    });
  }

  private loadClient(id: string): void {
    this.isLoading.set(true);
    this.clientService.getById(id).subscribe((client) => {
      this.currentStatus = client.status;
      this.form.patchValue(client);
      this.isLoading.set(false);
    });
  }
}
