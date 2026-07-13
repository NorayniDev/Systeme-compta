import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const SUPPLIERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/supplier-list/supplier-list').then((m) => m.SupplierList),
  },
  {
    path: 'new',
    canActivate: [permissionGuard(Permission.SUPPLIER_MANAGE)],
    loadComponent: () => import('./pages/supplier-form/supplier-form').then((m) => m.SupplierForm),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/supplier-detail/supplier-detail').then((m) => m.SupplierDetail),
  },
  {
    path: ':id/edit',
    canActivate: [permissionGuard(Permission.SUPPLIER_MANAGE)],
    loadComponent: () => import('./pages/supplier-form/supplier-form').then((m) => m.SupplierForm),
  },
];
