import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { TranslatePipe } from '@ngx-translate/core';
import { UserAccountService } from '../../services/user-account.service';
import { IUser, getFullName, getInitials } from '../../../../core/models/user.model';
import { Permission, ROLE_PERMISSIONS } from '../../../../core/constants/roles.constant';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';

/** Fiche de détail d'un utilisateur: profil, rôle, statut et permissions accordées. */
@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    TranslatePipe,
    RelativeTimePipe,
    HasPermissionDirective,
  ],
  templateUrl: './user-detail.html',
  styleUrl: './user-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserDetail implements OnInit {
  private readonly userAccountService = inject(UserAccountService);
  private readonly route = inject(ActivatedRoute);

  protected readonly Permission = Permission;
  protected readonly getFullName = getFullName;
  protected readonly getInitials = getInitials;
  protected readonly isLoading = signal(true);
  protected readonly user = signal<IUser | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.userAccountService.getById(id).subscribe((user) => {
      this.user.set(user);
      this.isLoading.set(false);
    });
  }

  protected grantedPermissions(user: IUser): readonly Permission[] {
    return user.permissions.length > 0 ? user.permissions : ROLE_PERMISSIONS[user.role];
  }
}
