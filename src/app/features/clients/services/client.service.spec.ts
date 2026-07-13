import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ClientService } from './client.service';
import { ClientStatus, IClient } from '../models/client.model';
import { DEFAULT_PAGE_REQUEST, IPage } from '../../../core/models/pagination.model';
import { environment } from '../../../../environments/environment';

describe('ClientService', () => {
  let service: ClientService;
  let httpMock: HttpTestingController;

  const mockClient: IClient = {
    id: '1',
    name: 'ACME SARL',
    email: 'contact@acme.sn',
    phone: '+221771234567',
    address: 'Dakar, Sénégal',
    taxId: 'NINEA123456',
    status: ClientStatus.ACTIVE,
    totalInvoiced: 1_500_000,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-01T00:00:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ClientService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ClientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a paginated list of clients', () => {
    const mockPage: IPage<IClient> = {
      content: [mockClient],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
      first: true,
      last: true,
    };

    service.search(DEFAULT_PAGE_REQUEST).subscribe((page) => {
      expect(page.content.length).toBe(1);
      expect(page.content[0].name).toBe('ACME SARL');
    });

    const req = httpMock.expectOne(
      (request) => request.url === `${environment.apiUrl}/clients` && request.method === 'GET',
    );
    req.flush(mockPage);
  });

  it('should fetch a client by id', () => {
    service.getById('1').subscribe((client) => {
      expect(client).toEqual(mockClient);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/clients/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockClient);
  });

  it('should create a client', () => {
    const dto = {
      name: 'ACME SARL',
      email: 'contact@acme.sn',
      phone: '+221771234567',
      address: 'Dakar, Sénégal',
      taxId: 'NINEA123456',
    };

    service.create(dto).subscribe((client) => {
      expect(client).toEqual(mockClient);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/clients`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(mockClient);
  });

  it('should delete a client', () => {
    service.delete('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/clients/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
