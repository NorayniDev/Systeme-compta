import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { QuoteService } from './quote.service';
import { IQuote, QuoteStatus } from '../models/quote.model';
import { environment } from '../../../../environments/environment';

describe('QuoteService', () => {
  let service: QuoteService;
  let httpMock: HttpTestingController;

  const mockQuote: IQuote = {
    id: '1',
    number: 'DEV-2026-0001',
    clientId: 'c1',
    clientName: 'ACME SARL',
    issueDate: '2026-01-01',
    validUntil: '2026-01-31',
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
    status: QuoteStatus.DRAFT,
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-01T00:00:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [QuoteService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(QuoteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a quote by id', () => {
    service.getById('1').subscribe((quote) => {
      expect(quote).toEqual(mockQuote);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/quotes/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockQuote);
  });

  it('should call the convert-to-invoice endpoint', () => {
    service.convertToInvoice('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/quotes/1/convert-to-invoice`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should delete a quote', () => {
    service.delete('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/quotes/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
