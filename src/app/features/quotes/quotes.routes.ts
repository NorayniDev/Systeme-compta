import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const QUOTES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/quote-list/quote-list').then((m) => m.QuoteList),
  },
  {
    path: 'new',
    canActivate: [permissionGuard(Permission.QUOTE_CREATE)],
    loadComponent: () => import('./pages/quote-form/quote-form').then((m) => m.QuoteForm),
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/quote-detail/quote-detail').then((m) => m.QuoteDetail),
  },
  {
    path: ':id/edit',
    canActivate: [permissionGuard(Permission.QUOTE_UPDATE)],
    loadComponent: () => import('./pages/quote-form/quote-form').then((m) => m.QuoteForm),
  },
];
