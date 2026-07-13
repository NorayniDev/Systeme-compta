import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PaymentService } from './payment.service';
import { IPayment, PaymentMethod, PaymentStatus } from '../models/payment.model';
import { environment } from '../../../../environments/environment';

describe('PaymentService', () => {
  let service: PaymentService;
  let httpMock: HttpTestingController;

  const mockPayment: IPayment = {
    id: '1',
    reference: 'PAY-2026-0001',
    invoiceId: 'i1',
    invoiceNumber: 'FAC-2026-0001',
    clientId: 'c1',
    clientName: 'ACME SARL',
    amount: 118_000,
    method: PaymentMethod.BANK_TRANSFER,
    status: PaymentStatus.COMPLETED,
    paidAt: '2026-01-05',
    createdAt: '2026-01-05T00:00:00.000Z',
    updatedAt: '2026-01-05T00:00:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PaymentService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PaymentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a payment', () => {
    const dto = {
      invoiceId: 'i1',
      amount: 118_000,
      method: PaymentMethod.BANK_TRANSFER,
      paidAt: '2026-01-05',
    };

    service.create(dto).subscribe((payment) => {
      expect(payment).toEqual(mockPayment);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/payments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(mockPayment);
  });

  it('should call the refund endpoint', () => {
    service.refund('1').subscribe((payment) => {
      expect(payment.status).toBe(PaymentStatus.COMPLETED);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/payments/1/refund`);
    expect(req.request.method).toBe('POST');
    req.flush(mockPayment);
  });

  it('should delete a payment', () => {
    service.delete('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/payments/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
