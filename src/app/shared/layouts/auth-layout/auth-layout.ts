import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { APP_CONSTANTS } from '../../../core/constants/app.constant';

/**
 * Layout centré utilisé par les pages publiques d'authentification
 * (connexion, mot de passe oublié, réinitialisation).
 */
@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterOutlet, MatIconModule],
  templateUrl: './auth-layout.html',
  styleUrl: './auth-layout.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthLayout {
  protected readonly appName = APP_CONSTANTS.APP_NAME;
}
