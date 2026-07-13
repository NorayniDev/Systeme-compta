import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { TranslatePipe } from '@ngx-translate/core';
import { SupplierService } from '../../services/supplier.service';
import { ISupplier } from '../../models/supplier.model';
import { CurrencyXofPipe } from '../../../../shared/pipes/currency-xof.pipe';
import { HasPermissionDirective } from '../../../../shared/directives/has-permission.directive';
import { Permission } from '../../../../core/constants/roles.constant';

/** Fiche de détail d'un fournisseur. Symétrique de `ClientDetail`. */
@Component({
  selector: 'app-supplier-detail',
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
  templateUrl: './supplier-detail.html',
  styleUrl: './supplier-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SupplierDetail implements OnInit {
  private readonly supplierService = inject(SupplierService);
  private readonly route = inject(ActivatedRoute);

  protected readonly Permission = Permission;
  protected readonly isLoading = signal(true);
  protected readonly supplier = signal<ISupplier | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.supplierService.getById(id).subscribe((supplier) => {
      this.supplier.set(supplier);
      this.isLoading.set(false);
    });
  }
}
