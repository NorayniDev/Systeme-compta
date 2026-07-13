import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { SupplierService } from './supplier.service';
import { ISupplier, SupplierStatus } from '../models/supplier.model';
import { DEFAULT_PAGE_REQUEST, IPage } from '../../../core/models/pagination.model';
import { environment } from '../../../../environments/environment';

describe('SupplierService', () => {
  let service: SupplierService;
  let httpMock: HttpTestingController;

  const mockSupplier: ISupplier = {
    id: '1',
    name: 'Sahel Fournitures SARL',
    email: 'contact@sahel-fournitures.sn',
    phone: '+221771112233',
    address: 'Dakar, Sénégal',
    taxId: 'NINEA9998887',
    status: SupplierStatus.ACTIVE,
    totalPurchased: 800_000,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-01T00:00:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SupplierService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SupplierService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a paginated list of suppliers', () => {
    const mockPage: IPage<ISupplier> = {
      content: [mockSupplier],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
      first: true,
      last: true,
    };

    service.search(DEFAULT_PAGE_REQUEST).subscribe((page) => {
      expect(page.content[0].name).toBe('Sahel Fournitures SARL');
    });

    const req = httpMock.expectOne(
      (request) => request.url === `${environment.apiUrl}/suppliers` && request.method === 'GET',
    );
    req.flush(mockPage);
  });

  it('should create a supplier', () => {
    const dto = {
      name: 'Sahel Fournitures SARL',
      email: 'contact@sahel-fournitures.sn',
      phone: '+221771112233',
      address: 'Dakar, Sénégal',
      taxId: 'NINEA9998887',
    };

    service.create(dto).subscribe((supplier) => {
      expect(supplier).toEqual(mockSupplier);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/suppliers`);
    expect(req.request.method).toBe('POST');
    req.flush(mockSupplier);
  });

  it('should delete a supplier', () => {
    service.delete('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/suppliers/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
