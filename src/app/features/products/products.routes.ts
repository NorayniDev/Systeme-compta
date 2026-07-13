import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const PRODUCTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/product-list/product-list').then((m) => m.ProductList),
  },
  {
    path: 'new',
    canActivate: [permissionGuard(Permission.PRODUCT_MANAGE)],
    loadComponent: () => import('./pages/product-form/product-form').then((m) => m.ProductForm),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/product-detail/product-detail').then((m) => m.ProductDetail),
  },
  {
    path: ':id/edit',
    canActivate: [permissionGuard(Permission.PRODUCT_MANAGE)],
    loadComponent: () => import('./pages/product-form/product-form').then((m) => m.ProductForm),
  },
];
