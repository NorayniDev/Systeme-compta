import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';
import { IApiError } from '../models/api-response.model';

/**
 * Traduit les erreurs HTTP en notifications utilisateur et redirige vers les
 * pages d'erreur dédiées pour les statuts structurants (403, 404, 500+).
 *
 * Le statut 401 est délibérément ignoré ici: il est entièrement pris en
 * charge par `authInterceptor` (rafraîchissement de session), qui est placé
 * plus proche du backend dans la chaîne — voir `app.config.ts`.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notificationService = inject(NotificationService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        handleHttpError(error, notificationService, router);
      } else {
        notificationService.error('Une erreur inattendue est survenue.');
      }
      return throwError(() => error);
    }),
  );
};

function handleHttpError(
  error: HttpErrorResponse,
  notificationService: NotificationService,
  router: Router,
): void {
  const apiError = error.error as Partial<IApiError> | undefined;
  const message = apiError?.message ?? error.message;

  switch (error.status) {
    case 0:
      notificationService.error('Impossible de contacter le serveur. Vérifiez votre connexion.');
      break;
    case 401:
      // Géré par authInterceptor.
      break;
    case 403:
      router.navigate(['/errors/403']);
      break;
    case 404:
      notificationService.warning(message || 'Ressource introuvable.');
      break;
    case 422:
    case 400:
      notificationService.warning(message || 'Certaines données saisies sont invalides.');
      break;
    default:
      if (error.status >= 500) {
        router.navigate(['/errors/500']);
      } else {
        notificationService.error(message || 'Une erreur est survenue.');
      }
  }
}
