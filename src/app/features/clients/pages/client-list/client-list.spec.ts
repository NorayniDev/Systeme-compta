import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { ClientList } from './client-list';
import { ClientService } from '../../services/client.service';
import { ClientStatus, IClient } from '../../models/client.model';
import { IPage } from '../../../../core/models/pagination.model';

describe('ClientList', () => {
  const mockPage: IPage<IClient> = {
    content: [
      {
        id: '1',
        name: 'ACME SARL',
        email: 'contact@acme.sn',
        phone: '+221771234567',
        address: 'Dakar',
        taxId: 'NINEA123456',
        status: ClientStatus.ACTIVE,
        totalInvoiced: 1_000_000,
        createdAt: '2026-01-01T00:00:00.000Z',
        updatedAt: '2026-01-01T00:00:00.000Z',
      },
    ],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 10,
    first: true,
    last: true,
  };

  const clientServiceStub = {
    search: jasmine.createSpy('search').and.returnValue(of(mockPage)),
    delete: jasmine.createSpy('delete').and.returnValue(of(undefined)),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        provideTranslateService(),
        { provide: ClientService, useValue: clientServiceStub },
      ],
    }).compileComponents();
  });

  it('should create and load the first page of clients on init', () => {
    const fixture = TestBed.createComponent(ClientList);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(clientServiceStub.search).toHaveBeenCalled();
    expect(fixture.componentInstance['clients']()).toEqual(mockPage.content);
    expect(fixture.componentInstance['totalElements']()).toBe(1);
  });
});
