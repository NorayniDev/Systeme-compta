import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuditService } from './audit.service';
import { IAuditLog, AuditAction } from '../models/audit-log.model';
import { IPage, DEFAULT_PAGE_REQUEST } from '../../../core/models/pagination.model';
import { environment } from '../../../../environments/environment';

describe('AuditService', () => {
  let service: AuditService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuditService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuditService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a page of audit logs', () => {
    const mockPage: IPage<IAuditLog> = {
      content: [
        {
          id: '1',
          timestamp: '2026-07-01T10:00:00Z',
          userName: 'admin',
          action: AuditAction.LOGIN,
          entityType: 'Auth',
          entityLabel: 'Connexion',
        },
      ],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
      first: true,
      last: true,
    };

    service.getLogs(DEFAULT_PAGE_REQUEST).subscribe((page) => {
      expect(page).toEqual(mockPage);
    });

    const req = httpMock.expectOne((request) => request.url === `${environment.apiUrl}/audit-logs`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });
});
