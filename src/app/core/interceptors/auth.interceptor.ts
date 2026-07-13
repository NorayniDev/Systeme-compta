import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, Observable, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthService } from '../authentication/auth.service';
import { Router } from '@angular/router';

const AUTH_FREE_PATHS = [
  'auth/login',
  'auth/refresh',
  'auth/forgot-password',
  'auth/reset-password',
];

// État de rafraîchissement partagé au niveau module: un seul appel de refresh
// en vol à la fois, les requêtes concurrentes attendent son résultat.
let isRefreshing = false;
const refreshedToken$ = new BehaviorSubject<string | null>(null);

/**
 * Attache le token JWT courant à chaque requête sortante et gère le
 * renouvellement automatique (auto refresh) en cas de réponse 401: la
 * requête initiale est rejouée une fois le nouveau token obtenu.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isAuthFreeEndpoint = AUTH_FREE_PATHS.some((path) => req.url.includes(path));
  const accessToken = authService.getAccessToken();

  const authorizedReq =
    accessToken && !isAuthFreeEndpoint
      ? req.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })
      : req;

  return next(authorizedReq).pipe(
    catchError((error: unknown) => {
      const isUnauthorized = error instanceof HttpErrorResponse && error.status === 401;
      if (!isUnauthorized || isAuthFreeEndpoint) {
        return throwError(() => error);
      }
      return handleUnauthorized(authorizedReq, next, authService, router);
    }),
  );
};

function handleUnauthorized(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
  authService: AuthService,
  router: Router,
): Observable<HttpEvent<unknown>> {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshedToken$.next(null);

    return authService.refreshAccessToken().pipe(
      switchMap((response) => {
        isRefreshing = false;
        refreshedToken$.next(response.accessToken);
        return next(req.clone({ setHeaders: { Authorization: `Bearer ${response.accessToken}` } }));
      }),
      catchError((refreshError: unknown) => {
        isRefreshing = false;
        authService.logout();
        router.navigate(['/auth/login']);
        return throwError(() => refreshError);
      }),
    );
  }

  // Une requête de rafraîchissement est déjà en cours: on attend son résultat.
  return refreshedToken$.pipe(
    filter((token): token is string => token !== null),
    take(1),
    switchMap((token) => next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }))),
  );
}
