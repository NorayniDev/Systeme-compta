import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../authentication/auth.service';
import { Permission } from '../constants/roles.constant';

/**
 * Fabrique de guard RBAC basé sur des permissions granulaires.
 * Usage: `canActivate: [permissionGuard(Permission.INVOICE_VALIDATE)]`
 */
export function permissionGuard(...requiredPermissions: Permission[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.hasPermission(...requiredPermissions)) {
      return true;
    }

    return router.createUrlTree(['/errors/403']);
  };
}
