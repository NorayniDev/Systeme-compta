import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const AUDIT_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard(Permission.AUDIT_READ)],
    loadComponent: () => import('./pages/audit-log/audit-log').then((m) => m.AuditLog),
  },
];
