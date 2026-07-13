import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { UserAccountService } from './user-account.service';
import { IUser } from '../../../core/models/user.model';
import { UserRole } from '../../../core/constants/roles.constant';
import { DEFAULT_PAGE_REQUEST, IPage } from '../../../core/models/pagination.model';
import { environment } from '../../../../environments/environment';

describe('UserAccountService', () => {
  let service: UserAccountService;
  let httpMock: HttpTestingController;

  const mockUser: IUser = {
    id: '1',
    firstName: 'Awa',
    lastName: 'Diop',
    email: 'awa.diop@facturation-pme.sn',
    role: UserRole.GESTIONNAIRE,
    permissions: [],
    isActive: true,
    createdAt: '2026-01-01T00:00:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserAccountService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserAccountService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a paginated list of users', () => {
    const mockPage: IPage<IUser> = {
      content: [mockUser],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
      first: true,
      last: true,
    };

    service.search(DEFAULT_PAGE_REQUEST).subscribe((page) => {
      expect(page.content[0].email).toBe('awa.diop@facturation-pme.sn');
    });

    const req = httpMock.expectOne(
      (request) => request.url === `${environment.apiUrl}/users` && request.method === 'GET',
    );
    req.flush(mockPage);
  });

  it('should call the reset-password endpoint', () => {
    service.resetPassword('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/users/1/reset-password`);
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });

  it('should delete a user', () => {
    service.delete('1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/users/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
