import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { InvoiceService } from './invoice.service';
import { InvoiceStatus, IInvoice } from '../models/invoice.model';
import { environment } from '../../../../environments/environment';

describe('InvoiceService', () => {
  let service: InvoiceService;
  let httpMock: HttpTestingController;

  const mockInvoice: IInvoice = {
    id: '1',
    number: 'FAC-2026-0001',
    clientId: 'c1',
    clientName: 'ACME SARL',
    issueDate: '2026-01-01',
    dueDate: '2026-01-31',
    lines: [
      {
        description: 'Prestation',
        quantity: 1,
        unitPrice: 100_000,
        taxRate: 18,
        lineTotal: 118_000,
      },
    ],
    amountExclTax: 100_000,
    taxAmount: 18_000,
    totalAmount: 118_000,
    status: InvoiceStatus.DRAFT,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-01T00:00:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InvoiceService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(InvoiceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch an invoice by id', () => {
    service.getById('1').subscribe((invoice) => {
      expect(invoice).toEqual(mockInvoice);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/invoices/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockInvoice);
  });

  it('should call the validate endpoint', () => {
    service.validate('1').subscribe((invoice) => {
      expect(invoice.status).toBe(InvoiceStatus.DRAFT);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/invoices/1/validate`);
    expect(req.request.method).toBe('POST');
    req.flush(mockInvoice);
  });

  it('should delete an invoice', () => {
    service.delete('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/invoices/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
