import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';
import { UserRole } from '../../core/constants/roles.constant';

export const ROLES_ROUTES: Routes = [
  {
    path: '',
    canActivate: [roleGuard(UserRole.ADMIN)],
    loadComponent: () => import('./pages/role-matrix/role-matrix').then((m) => m.RoleMatrix),
  },
];
