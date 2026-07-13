import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { SettingsService } from './settings.service';
import { IUser } from '../../../core/models/user.model';
import { UserRole } from '../../../core/constants/roles.constant';
import { ICompanySettings } from '../models/company-settings.model';
import { environment } from '../../../../environments/environment';

describe('SettingsService', () => {
  let service: SettingsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SettingsService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SettingsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should update the profile', () => {
    const mockUser: IUser = {
      id: '1',
      firstName: 'Admin',
      lastName: 'Démo',
      email: 'admin@facturation-pme.sn',
      role: UserRole.ADMIN,
      permissions: [],
      isActive: true,
      createdAt: '2026-01-01T00:00:00.000Z',
    };

    service
      .updateProfile({ firstName: 'Admin', lastName: 'Démo', email: 'admin@facturation-pme.sn' })
      .subscribe((user) => {
        expect(user).toEqual(mockUser);
      });

    const req = httpMock.expectOne(`${environment.apiUrl}/profile`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockUser);
  });

  it('should change the password', () => {
    service.changePassword({ currentPassword: 'old', newPassword: 'newPassword123' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/profile/change-password`);
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });

  it('should fetch company settings', () => {
    const mockSettings: ICompanySettings = {
      companyName: 'FacturationPME SARL',
      address: 'Dakar, Sénégal',
      taxId: 'NINEA0000000',
      currency: 'XOF',
      defaultTaxRate: 18,
      invoicePrefix: 'FAC',
      quotePrefix: 'DEV',
    };

    service.getCompanySettings().subscribe((settings) => {
      expect(settings).toEqual(mockSettings);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/settings/company`);
    expect(req.request.method).toBe('GET');
    req.flush(mockSettings);
  });
});
