import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { TranslatePipe } from '@ngx-translate/core';
import { ClientService } from '../../services/client.service';
import { IClient } from '../../models/client.model';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';

/**
 * Fiche de détail d'un client: informations générales et historique de
 * facturation (à raccorder au module Factures via `client.id`).
 */
@Component({
  selector: 'app-client-detail',
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
  templateUrl: './client-detail.html',
  styleUrl: './client-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientDetail implements OnInit {
  private readonly clientService = inject(ClientService);
  private readonly route = inject(ActivatedRoute);

  protected readonly Permission = Permission;
  protected readonly isLoading = signal(true);
  protected readonly client = signal<IClient | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.clientService.getById(id).subscribe((client) => {
      this.client.set(client);
      this.isLoading.set(false);
    });
  }
}
