import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { TranslatePipe } from '@ngx-translate/core';
import { ServiceItemService } from '../../services/service-item.service';
import { IServiceItem } from '../../models/service-item.model';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';

/** Fiche de détail d'une prestation. */
@Component({
  selector: 'app-service-detail',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    TranslatePipe,
    CurrencyXofPipe,
    HasPermissionDirective,
  ],
  templateUrl: './service-detail.html',
  styleUrl: './service-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServiceDetail implements OnInit {
  private readonly serviceItemService = inject(ServiceItemService);
  private readonly route = inject(ActivatedRoute);

  protected readonly Permission = Permission;
  protected readonly isLoading = signal(true);
  protected readonly serviceItem = signal<IServiceItem | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.serviceItemService.getById(id).subscribe((serviceItem) => {
      this.serviceItem.set(serviceItem);
      this.isLoading.set(false);
    });
  }
}
