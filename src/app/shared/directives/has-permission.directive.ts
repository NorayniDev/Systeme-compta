import { Directive, effect, inject, input, TemplateRef, ViewContainerRef } from '@angular/core';
import { AuthService } from '../../core/authentication/auth.service';
import { Permission } from '../../core/constants/roles.constant';

/**
 * Affiche le contenu hébergé uniquement si l'utilisateur courant possède
 * toutes les permissions indiquées.
 * Usage: `<button *appHasPermission="[Permission.INVOICE_VALIDATE]">...</button>`
 */
@Directive({
  selector: '[appHasPermission]',
  standalone: true,
})
export class HasPermissionDirective {
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainer = inject(ViewContainerRef);
  private readonly authService = inject(AuthService);

  readonly appHasPermission = input.required<Permission[]>();

  private hasView = false;

  constructor() {
    effect(() => {
      const allowed = this.authService.hasPermission(...this.appHasPermission());
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
