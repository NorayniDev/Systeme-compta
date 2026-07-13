import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/guards/permission.guard';
import { Permission } from '../../core/constants/roles.constant';

/** Toutes les vues comptables exigent au minimum la lecture (`ACCOUNTING_READ`). */
export const ACCOUNTING_ROUTES: Routes = [
  {
    path: 'journal',
    canActivate: [permissionGuard(Permission.ACCOUNTING_READ)],
    loadComponent: () => import('./pages/journal/journal').then((m) => m.Journal),
  },
  {
    path: 'ledger',
    canActivate: [permissionGuard(Permission.ACCOUNTING_READ)],
    loadComponent: () => import('./pages/ledger/ledger').then((m) => m.Ledger),
  },
  {
    path: 'trial-balance',
    canActivate: [permissionGuard(Permission.ACCOUNTING_READ)],
    loadComponent: () => import('./pages/trial-balance/trial-balance').then((m) => m.TrialBalance),
  },
  {
    path: 'cashbook',
    canActivate: [permissionGuard(Permission.ACCOUNTING_READ)],
    loadComponent: () => import('./pages/cashbook/cashbook').then((m) => m.Cashbook),
  },
  { path: '', pathMatch: 'full', redirectTo: 'journal' },
];
