import { Directive, effect, inject, input, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthService } from '../../core/authentication/auth.service';
import { UserRole } from '../../core/constants/roles.constant';

/**
 * Affiche le contenu hébergé uniquement si l'utilisateur courant possède
 * l'un des rôles indiqués. Usage: `<div *appHasRole="[UserRole.ADMIN]">...</div>`
 */
@Directive({
  selector: '[appHasRole]',
  standalone: true,
})
export class HasRoleDirective {
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainer = inject(ViewContainerRef);
  private readonly authService = inject(AuthService);

  readonly appHasRole = input.required<UserRole[]>();

  private hasView = false;

  constructor() {
    effect(() => {
      const allowed = this.authService.hasRole(...this.appHasRole());
      if (allowed && !this.hasView) {
        this.viewContainer.createEmbeddedView(this.templateRef);
        this.hasView = true;
      } else if (!allowed && this.hasView) {
        this.viewContainer.clear();
        this.hasView = false;
      }
    });
  }
}
