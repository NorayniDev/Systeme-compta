import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../authentication/auth.service';
import { UserRole } from '../constants/roles.constant';

/**
 * Fabrique de guard RBAC basé sur le rôle.
 * Usage: `canActivate: [roleGuard(UserRole.ADMIN, UserRole.COMPTABLE)]`
 */
export function roleGuard(...allowedRoles: UserRole[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.hasRole(...allowedRoles)) {
      return true;
    }

    return router.createUrlTree(['/errors/403']);
  };
}
