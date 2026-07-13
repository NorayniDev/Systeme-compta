import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const SERVICES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/service-list/service-list').then((m) => m.ServiceList),
  },
  {
    path: 'new',
    canActivate: [permissionGuard(Permission.SERVICE_MANAGE)],
    loadComponent: () => import('./pages/service-form/service-form').then((m) => m.ServiceForm),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/service-detail/service-detail').then((m) => m.ServiceDetail),
  },
  {
    path: ':id/edit',
    canActivate: [permissionGuard(Permission.SERVICE_MANAGE)],
    loadComponent: () => import('./pages/service-form/service-form').then((m) => m.ServiceForm),
  },
];
