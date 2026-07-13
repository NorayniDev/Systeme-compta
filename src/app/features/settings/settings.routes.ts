import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/company-settings/company-settings').then((m) => m.CompanySettings),
    canActivate: [permissionGuard(Permission.SETTINGS_MANAGE)],
  },
  {
    path: 'profile',
    loadComponent: () => import('./pages/profile/profile').then((m) => m.Profile),
  },
];
