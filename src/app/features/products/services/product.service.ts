import { Injectable } from '@angular/core';
import { BaseService } from '../../../core/services/base.service';
import { API_ENDPOINTS } from '../../../core/constants/api-endpoints.constant';
import { IProduct } from '../models/product.model';
import { ProductCreateDto, ProductUpdateDto } from '../models/product.dto';

/**
 * Service métier du domaine Produit. Hérite de toutes les opérations
 * CRUD génériques de `BaseService`.
 */
@Injectable({ providedIn: 'root' })
export class ProductService extends BaseService<IProduct, ProductCreateDto, ProductUpdateDto> {
  protected readonly resourcePath = API_ENDPOINTS.PRODUCTS;
}
