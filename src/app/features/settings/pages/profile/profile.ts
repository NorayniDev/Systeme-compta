import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { SettingsService } from '../../services/settings.service';
import { AuthService } from '../../../../core/authentication/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { getFullName, getInitials } from '../../../../core/models/user.model';
import { passwordsMatchValidator } from '../../../../shared/validators/password.validators';

/**
 * Page Profil: informations personnelles de l'utilisateur courant et
 * changement de mot de passe. Toute modification réussie met à jour
 * `AuthService.currentUser` pour se refléter immédiatement dans l'UI
 * (avatar, nom affiché dans la barre supérieure).
 */
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    TranslatePipe,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Profile {
  private readonly formBuilder = inject(FormBuilder);
  private readonly settingsService = inject(SettingsService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  protected readonly authService = inject(AuthService);

  protected readonly getFullName = getFullName;
  protected readonly getInitials = getInitials;

  protected readonly isSavingProfile = signal(false);
  protected readonly isChangingPassword = signal(false);

  protected readonly profileForm = this.formBuilder.nonNullable.group({
    firstName: [
      this.authService.currentUser()?.firstName ?? '',
      [Validators.required, Validators.minLength(2)],
    ],
    lastName: [
      this.authService.currentUser()?.lastName ?? '',
      [Validators.required, Validators.minLength(2)],
    ],
    email: [this.authService.currentUser()?.email ?? '', [Validators.required, Validators.email]],
  });

  protected readonly passwordForm = this.formBuilder.nonNullable.group(
    {
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmNewPassword: ['', Validators.required],
    },
    { validators: passwordsMatchValidator('newPassword', 'confirmNewPassword') },
  );

  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isSavingProfile.set(true);
    const dto = this.profileForm.getRawValue();

    this.settingsService
      .updateProfile(dto)
      .pipe(finalize(() => this.isSavingProfile.set(false)))
      .subscribe((user) => {
        this.authService.updateCurrentUser(user);
        this.notificationService.success(
          this.translateService.instant('settings.profile.updatedSuccess'),
        );
      });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.isChangingPassword.set(true);
    const { currentPassword, newPassword } = this.passwordForm.getRawValue();

    this.settingsService
      .changePassword({ currentPassword, newPassword })
      .pipe(finalize(() => this.isChangingPassword.set(false)))
      .subscribe(() => {
        this.notificationService.success(
          this.translateService.instant('settings.profile.passwordChangedSuccess'),
        );
        this.passwordForm.reset();
      });
  }
}
