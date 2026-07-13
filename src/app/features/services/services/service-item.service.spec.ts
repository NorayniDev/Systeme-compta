import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ServiceItemService } from './service-item.service';
import { IServiceItem, ServiceItemStatus, ServiceItemUnit } from '../models/service-item.model';
import { DEFAULT_PAGE_REQUEST, IPage } from '../../../core/models/pagination.model';
import { environment } from '../../../../environments/environment';

describe('ServiceItemService', () => {
  let service: ServiceItemService;
  let httpMock: HttpTestingController;

  const mockServiceItem: IServiceItem = {
    id: '1',
    name: 'Consultation stratégique',
    code: 'SRV-0001',
    description: "Séance de conseil en stratégie d'entreprise",
    category: 'Conseil',
    unitPrice: 75_000,
    taxRate: 18,
    unit: ServiceItemUnit.HOUR,
    status: ServiceItemStatus.ACTIVE,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-01T00:00:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ServiceItemService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ServiceItemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a paginated list of service items', () => {
    const mockPage: IPage<IServiceItem> = {
      content: [mockServiceItem],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
      first: true,
      last: true,
    };

    service.search(DEFAULT_PAGE_REQUEST).subscribe((page) => {
      expect(page.content[0].name).toBe('Consultation stratégique');
    });

    const req = httpMock.expectOne(
      (request) => request.url === `${environment.apiUrl}/services` && request.method === 'GET',
    );
    req.flush(mockPage);
  });

  it('should create a service item', () => {
    const dto = {
      name: 'Consultation stratégique',
      code: 'SRV-0001',
      description: "Séance de conseil en stratégie d'entreprise",
      category: 'Conseil',
      unitPrice: 75_000,
      taxRate: 18,
      unit: ServiceItemUnit.HOUR,
    };

    service.create(dto).subscribe((item) => {
      expect(item).toEqual(mockServiceItem);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/services`);
    expect(req.request.method).toBe('POST');
    req.flush(mockServiceItem);
  });

  it('should delete a service item', () => {
    service.delete('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
