import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ReportService } from './report.service';
import { ISalesByClientLine, IAgingReceivableLine } from '../models/report.model';
import { environment } from '../../../../environments/environment';

describe('ReportService', () => {
  let service: ReportService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ReportService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ReportService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch sales by client', () => {
    const mockLines: ISalesByClientLine[] = [
      {
        clientId: '1',
        clientName: 'ACME SARL',
        invoiceCount: 2,
        amountExclTax: 100_000,
        taxAmount: 18_000,
        totalAmount: 118_000,
      },
    ];

    service.getSalesByClient({}).subscribe((lines) => {
      expect(lines).toEqual(mockLines);
    });

    const req = httpMock.expectOne(
      (request) => request.url === `${environment.apiUrl}/reports/sales-by-client`,
    );
    req.flush(mockLines);
  });

  it('should fetch aging receivables', () => {
    const mockLines: IAgingReceivableLine[] = [
      {
        invoiceId: '1',
        invoiceNumber: 'FAC-2026-0004',
        clientName: 'ACME SARL',
        dueDate: '2026-04-30',
        daysOverdue: 30,
        amountDue: 1_062_000,
      },
    ];

    service.getAgingReceivables().subscribe((lines) => {
      expect(lines).toEqual(mockLines);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/reports/aging-receivables`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLines);
  });
});
