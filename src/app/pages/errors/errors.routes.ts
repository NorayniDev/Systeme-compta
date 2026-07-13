import { Routes } from '@angular/router';
import { ErrorPage } from './error-page/error-page';

/**
 * Routes des pages d'erreur. `titleKey`/`messageKey` sont des clés
 * ngx-translate liées aux `input()` du composant via `withComponentInputBinding()`.
 */
export const ERRORS_ROUTES: Routes = [
  {
    path: '401',
    component: ErrorPage,
    data: {
      code: '401',
      titleKey: 'errors.401.title',
      messageKey: 'errors.401.message',
      icon: 'lock_outline',
    },
  },
  {
    path: '403',
    component: ErrorPage,
    data: {
      code: '403',
      titleKey: 'errors.403.title',
      messageKey: 'errors.403.message',
      icon: 'block',
    },
  },
  {
    path: '404',
    component: ErrorPage,
    data: {
      code: '404',
      titleKey: 'errors.404.title',
      messageKey: 'errors.404.message',
      icon: 'search_off',
    },
  },
  {
    path: '500',
    component: ErrorPage,
    data: {
      code: '500',
      titleKey: 'errors.500.title',
      messageKey: 'errors.500.message',
      icon: 'dns',
    },
  },
  { path: '', redirectTo: '404', pathMatch: 'full' },
];
