import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

export const INVOICES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/invoice-list/invoice-list').then((m) => m.InvoiceList),
  },
  {
    path: 'new',
    canActivate: [permissionGuard(Permission.INVOICE_CREATE)],
    loadComponent: () => import('./pages/invoice-form/invoice-form').then((m) => m.InvoiceForm),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/invoice-detail/invoice-detail').then((m) => m.InvoiceDetail),
  },
  {
    path: ':id/edit',
    canActivate: [permissionGuard(Permission.INVOICE_UPDATE)],
    loadComponent: () => import('./pages/invoice-form/invoice-form').then((m) => m.InvoiceForm),
  },
];
