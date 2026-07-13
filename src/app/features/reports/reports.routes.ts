import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const REPORTS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard(Permission.REPORT_READ)],
    loadComponent: () => import('./pages/reports/reports').then((m) => m.Reports),
  },
];
