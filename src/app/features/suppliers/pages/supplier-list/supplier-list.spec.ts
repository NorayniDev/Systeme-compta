import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { SupplierList } from './supplier-list';
import { SupplierService } from '../../services/supplier.service';
import { ISupplier, SupplierStatus } from '../../models/supplier.model';
import { IPage } from '../../../../core/models/pagination.model';

describe('SupplierList', () => {
  const mockPage: IPage<ISupplier> = {
    content: [
      {
        id: '1',
        name: 'Sahel Fournitures SARL',
        email: 'contact@sahel-fournitures.sn',
        phone: '+221771112233',
        address: 'Dakar',
        taxId: 'NINEA9998887',
        status: SupplierStatus.ACTIVE,
        totalPurchased: 800_000,
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

  const supplierServiceStub = {
    search: jasmine.createSpy('search').and.returnValue(of(mockPage)),
    delete: jasmine.createSpy('delete').and.returnValue(of(undefined)),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupplierList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        provideTranslateService(),
        { provide: SupplierService, useValue: supplierServiceStub },
      ],
    }).compileComponents();
  });

  it('should create and load the first page of suppliers on init', () => {
    const fixture = TestBed.createComponent(SupplierList);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(supplierServiceStub.search).toHaveBeenCalled();
    expect(fixture.componentInstance['suppliers']()).toEqual(mockPage.content);
    expect(fixture.componentInstance['totalElements']()).toBe(1);
  });
});
