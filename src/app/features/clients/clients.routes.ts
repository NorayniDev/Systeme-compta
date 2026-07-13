import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const CLIENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/client-list/client-list').then((m) => m.ClientList),
  },
  {
    path: 'new',
    canActivate: [permissionGuard(Permission.CLIENT_MANAGE)],
    loadComponent: () => import('./pages/client-form/client-form').then((m) => m.ClientForm),
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/client-detail/client-detail').then((m) => m.ClientDetail),
  },
  {
    path: ':id/edit',
    canActivate: [permissionGuard(Permission.CLIENT_MANAGE)],
    loadComponent: () => import('./pages/client-form/client-form').then((m) => m.ClientForm),
  },
];
