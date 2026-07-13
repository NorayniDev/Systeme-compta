import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { UserList } from './user-list';
import { UserAccountService } from '../../services/user-account.service';
import { IUser } from '../../../../core/models/user.model';
import { UserRole } from '../../../../core/constants/roles.constant';
import { IPage } from '../../../../core/models/pagination.model';

describe('UserList', () => {
  const mockPage: IPage<IUser> = {
    content: [
      {
        id: '1',
        firstName: 'Awa',
        lastName: 'Diop',
        email: 'awa.diop@facturation-pme.sn',
        role: UserRole.GESTIONNAIRE,
        permissions: [],
        isActive: true,
        createdAt: '2026-01-01T00:00:00.000Z',
      },
    ],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 10,
    first: true,
    last: true,
  };

  const userAccountServiceStub = {
    search: jasmine.createSpy('search').and.returnValue(of(mockPage)),
    delete: jasmine.createSpy('delete').and.returnValue(of(undefined)),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        provideTranslateService(),
        { provide: UserAccountService, useValue: userAccountServiceStub },
      ],
    }).compileComponents();
  });

  it('should create and load the first page of users on init', () => {
    const fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(userAccountServiceStub.search).toHaveBeenCalled();
    expect(fixture.componentInstance['users']()).toEqual(mockPage.content);
    expect(fixture.componentInstance['totalElements']()).toBe(1);
  });
});
