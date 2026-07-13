import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

/**
 * Page d'erreur générique et réutilisable (404, 401, 403, 500).
 * Le contenu (code, clés de titre/message, icône) provient des `data` de
 * route, liées automatiquement via `withComponentInputBinding()` — voir `errors.routes.ts`.
 */
@Component({
  selector: 'app-error-page',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule, TranslatePipe],
  templateUrl: './error-page.html',
  styleUrl: './error-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ErrorPage {
  readonly code = input.required<string>();
  readonly titleKey = input.required<string>();
  readonly messageKey = input.required<string>();
  readonly icon = input<string>('error_outline');
}
