import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { UserAccountService } from '../../services/user-account.service';
import { UserRole } from '../../../../core/constants/roles.constant';
import { NotificationService } from '../../../../core/services/notification.service';

/**
 * Formulaire de création/édition d'un utilisateur.
 * Le mot de passe temporaire n'est demandé qu'à la création — une fois le
 * compte créé, seule l'action "Réinitialiser le mot de passe" (liste/détail)
 * permet d'en générer un nouveau.
 */
@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TranslatePipe,
  ],
  templateUrl: './user-form.html',
  styleUrl: './user-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserForm implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly userAccountService = inject(UserAccountService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly isLoading = signal(false);
  protected readonly userId = signal<string | null>(null);
  protected readonly isEditMode = signal(false);
  protected readonly roles = Object.values(UserRole);

  protected readonly form = this.formBuilder.nonNullable.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    role: [UserRole.UTILISATEUR, Validators.required],
    temporaryPassword: ['', [Validators.required, Validators.minLength(8)]],
    isActive: [true],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.userId.set(id);
      this.isEditMode.set(true);
      this.form.controls.temporaryPassword.clearValidators();
      this.form.controls.temporaryPassword.updateValueAndValidity();
      this.loadUser(id);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const raw = this.form.getRawValue();
    const id = this.userId();

    const request$ = id
      ? this.userAccountService.update(id, {
          firstName: raw.firstName,
          lastName: raw.lastName,
          email: raw.email,
          role: raw.role,
          isActive: raw.isActive,
        })
      : this.userAccountService.create({
          firstName: raw.firstName,
          lastName: raw.lastName,
          email: raw.email,
          role: raw.role,
          temporaryPassword: raw.temporaryPassword,
        });

    request$.pipe(finalize(() => this.isSubmitting.set(false))).subscribe(() => {
      const messageKey = id ? 'users.updatedSuccess' : 'users.createdSuccess';
      this.notificationService.success(this.translateService.instant(messageKey));
      this.router.navigate(['/users']);
    });
  }

  private loadUser(id: string): void {
    this.isLoading.set(true);
    this.userAccountService.getById(id).subscribe((user) => {
      this.form.patchValue(user);
      this.isLoading.set(false);
    });
  }
}
