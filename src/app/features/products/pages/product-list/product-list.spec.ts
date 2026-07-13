import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { ProductList } from './product-list';
import { ProductService } from '../../services/product.service';
import { IProduct, ProductStatus, ProductUnit } from '../../models/product.model';
import { IPage } from '../../../../core/models/pagination.model';

describe('ProductList', () => {
  const mockPage: IPage<IProduct> = {
    content: [
      {
        id: '1',
        name: 'Ordinateur portable 15"',
        sku: 'PRD-0001',
        description: 'Ordinateur portable professionnel',
        category: 'Informatique',
        unitPrice: 450_000,
        taxRate: 18,
        unit: ProductUnit.PIECE,
        stockQuantity: 12,
        status: ProductStatus.ACTIVE,
        createdAt: '2026-01-01T00:00:00.000Z',
        updatedAt: '2026-01-01T00:00:00.000Z',
      },
    ],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 10,
    first: true,
    last: true,
  };

  const productServiceStub = {
    search: jasmine.createSpy('search').and.returnValue(of(mockPage)),
    delete: jasmine.createSpy('delete').and.returnValue(of(undefined)),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        provideTranslateService(),
        { provide: ProductService, useValue: productServiceStub },
      ],
    }).compileComponents();
  });

  it('should create and load the first page of products on init', () => {
    const fixture = TestBed.createComponent(ProductList);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(productServiceStub.search).toHaveBeenCalled();
    expect(fixture.componentInstance['products']()).toEqual(mockPage.content);
    expect(fixture.componentInstance['totalElements']()).toBe(1);
  });
});
