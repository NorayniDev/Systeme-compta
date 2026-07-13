import { ErrorHandler, Injectable, NgZone, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NotificationService } from '../services/notification.service';

/**
 * Filet de sécurité pour toute erreur JavaScript non interceptée
 * (exceptions dans les templates, erreurs de rendu, code tiers, ...).
 * Les erreurs HTTP sont ignorées ici: elles sont déjà traitées par
 * `errorInterceptor`/`authInterceptor` et remonteraient sinon deux fois.
 */
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private readonly notificationService = inject(NotificationService);
  private readonly zone = inject(NgZone);

  handleError(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      return;
    }

    console.error('[GlobalErrorHandler]', error);

    this.zone.run(() => {
      this.notificationService.error("Une erreur inattendue s'est produite dans l'application.");
    });
  }
}
