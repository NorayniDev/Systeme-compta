import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const PAYMENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/payment-list/payment-list').then((m) => m.PaymentList),
  },
  {
    path: 'new',
    canActivate: [permissionGuard(Permission.PAYMENT_CREATE)],
    loadComponent: () => import('./pages/payment-form/payment-form').then((m) => m.PaymentForm),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/payment-detail/payment-detail').then((m) => m.PaymentDetail),
  },
];
