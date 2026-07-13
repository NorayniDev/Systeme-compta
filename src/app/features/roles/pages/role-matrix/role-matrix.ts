import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslatePipe } from '@ngx-translate/core';
import { Permission, ROLE_PERMISSIONS, UserRole } from '../../../../core/constants/roles.constant';

interface IPermissionRow {
  permission: Permission;
  category: string;
  grantedByRole: Record<UserRole, boolean>;
}

/**
 * Vue de consultation de la matrice rôle → permissions.
 *
 * Contrairement aux autres modules d'administration, les rôles ne sont PAS
 * gérables en CRUD ici : ils forment un ensemble fixe (`UserRole`) codé en
 * dur et vérifié à la compilation par les guards/directives RBAC
 * (`roleGuard`, `*appHasRole`, ...). Autoriser la création de rôles
 * arbitraires casserait cette garantie de typage. Cette page documente donc
 * simplement, de façon lisible, ce que chaque rôle peut faire.
 */
@Component({
  selector: 'app-role-matrix',
  standalone: true,
  imports: [MatTableModule, MatIconModule, MatTooltipModule, TranslatePipe],
  templateUrl: './role-matrix.html',
  styleUrl: './role-matrix.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleMatrix {
  protected readonly roles = Object.values(UserRole);
  protected readonly displayedColumns = ['permission', ...this.roles];

  protected readonly rows: IPermissionRow[] = Object.values(Permission).map((permission) => ({
    permission,
    category: permission.split(':')[0],
    grantedByRole: Object.fromEntries(
      this.roles.map((role) => [role, ROLE_PERMISSIONS[role].includes(permission)]),
    ) as Record<UserRole, boolean>,
  }));

  protected readonly categories = Array.from(new Set(this.rows.map((row) => row.category)));

  protected rowsForCategory(category: string): IPermissionRow[] {
    return this.rows.filter((row) => row.category === category);
  }
}
