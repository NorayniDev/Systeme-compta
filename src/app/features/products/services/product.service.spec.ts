import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ProductService } from './product.service';
import { IProduct, ProductStatus, ProductUnit } from '../models/product.model';
import { DEFAULT_PAGE_REQUEST, IPage } from '../../../core/models/pagination.model';
import { environment } from '../../../../environments/environment';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  const mockProduct: IProduct = {
    id: '1',
    name: 'Ordinateur portable 15"',
    sku: 'PRD-0001',
    description: 'Ordinateur portable professionnel 15 pouces',
    category: 'Informatique',
    unitPrice: 450_000,
    taxRate: 18,
    unit: ProductUnit.PIECE,
    stockQuantity: 12,
    status: ProductStatus.ACTIVE,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-01T00:00:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProductService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a paginated list of products', () => {
    const mockPage: IPage<IProduct> = {
      content: [mockProduct],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
      first: true,
      last: true,
    };

    service.search(DEFAULT_PAGE_REQUEST).subscribe((page) => {
      expect(page.content[0].name).toBe('Ordinateur portable 15"');
    });

    const req = httpMock.expectOne(
      (request) => request.url === `${environment.apiUrl}/products` && request.method === 'GET',
    );
    req.flush(mockPage);
  });

  it('should create a product', () => {
    const dto = {
      name: 'Ordinateur portable 15"',
      sku: 'PRD-0001',
      description: 'Ordinateur portable professionnel 15 pouces',
      category: 'Informatique',
      unitPrice: 450_000,
      taxRate: 18,
      unit: ProductUnit.PIECE,
      stockQuantity: 12,
    };

    service.create(dto).subscribe((product) => {
      expect(product).toEqual(mockProduct);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/products`);
    expect(req.request.method).toBe('POST');
    req.flush(mockProduct);
  });

  it('should delete a product', () => {
    service.delete('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/products/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
