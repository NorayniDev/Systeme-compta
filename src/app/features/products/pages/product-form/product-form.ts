import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { ProductService } from '../../services/product.service';
import { ProductStatus, ProductUnit } from '../../models/product.model';
import { NotificationService } from '../../../../core/services/notification.service';

/**
 * Formulaire de création/édition d'un produit.
 * Le mode est déterminé par la présence d'un `id` dans la route parente.
 */
@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TranslatePipe,
  ],
  templateUrl: './product-form.html',
  styleUrl: './product-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductForm implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly productService = inject(ProductService);
  private readonly notificationService = inject(NotificationService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly isLoading = signal(false);
  protected readonly productId = signal<string | null>(null);
  protected readonly isEditMode = signal(false);
  protected readonly units = Object.values(ProductUnit);
  private currentStatus: ProductStatus = ProductStatus.ACTIVE;

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    sku: ['', [Validators.required, Validators.maxLength(30)]],
    description: [''],
    category: ['', Validators.required],
    unitPrice: [0, [Validators.required, Validators.min(0)]],
    taxRate: [18, [Validators.required, Validators.min(0), Validators.max(100)]],
    unit: [ProductUnit.PIECE, Validators.required],
    stockQuantity: [0, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.productId.set(id);
      this.isEditMode.set(true);
      this.loadProduct(id);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const dto = this.form.getRawValue();
    const id = this.productId();

    const request$ = id
      ? this.productService.update(id, { ...dto, status: this.currentStatus })
      : this.productService.create(dto);

    request$.pipe(finalize(() => this.isSubmitting.set(false))).subscribe(() => {
      const messageKey = id ? 'products.updatedSuccess' : 'products.createdSuccess';
      this.notificationService.success(this.translateService.instant(messageKey));
      this.router.navigate(['/products']);
    });
  }

  private loadProduct(id: string): void {
    this.isLoading.set(true);
    this.productService.getById(id).subscribe((product) => {
      this.currentStatus = product.status;
      this.form.patchValue(product);
      this.isLoading.set(false);
    });
  }
}
