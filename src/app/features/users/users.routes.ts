import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const USERS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permissionGuard(Permission.USER_MANAGE)],
    loadComponent: () => import('./pages/user-list/user-list').then((m) => m.UserList),
  },
  {
    path: 'new',
    canActivate: [permissionGuard(Permission.USER_MANAGE)],
    loadComponent: () => import('./pages/user-form/user-form').then((m) => m.UserForm),
  },
  {
    path: ':id',
    canActivate: [permissionGuard(Permission.USER_MANAGE)],
    loadComponent: () => import('./pages/user-detail/user-detail').then((m) => m.UserDetail),
  },
  {
    path: ':id/edit',
    canActivate: [permissionGuard(Permission.USER_MANAGE)],
    loadComponent: () => import('./pages/user-form/user-form').then((m) => m.UserForm),
  },
];
