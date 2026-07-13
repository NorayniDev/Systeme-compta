import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { ServiceList } from './service-list';
import { ServiceItemService } from '../../services/service-item.service';
import { IServiceItem, ServiceItemStatus, ServiceItemUnit } from '../../models/service-item.model';
import { IPage } from '../../../../core/models/pagination.model';

describe('ServiceList', () => {
  const mockPage: IPage<IServiceItem> = {
    content: [
      {
        id: '1',
        name: 'Consultation stratégique',
        code: 'SRV-0001',
        description: "Séance de conseil en stratégie d'entreprise",
        category: 'Conseil',
        unitPrice: 75_000,
        taxRate: 18,
        unit: ServiceItemUnit.HOUR,
        status: ServiceItemStatus.ACTIVE,
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

  const serviceItemServiceStub = {
    search: jasmine.createSpy('search').and.returnValue(of(mockPage)),
    delete: jasmine.createSpy('delete').and.returnValue(of(undefined)),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServiceList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        provideTranslateService(),
        { provide: ServiceItemService, useValue: serviceItemServiceStub },
      ],
    }).compileComponents();
  });

  it('should create and load the first page of service items on init', () => {
    const fixture = TestBed.createComponent(ServiceList);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(serviceItemServiceStub.search).toHaveBeenCalled();
    expect(fixture.componentInstance['serviceItems']()).toEqual(mockPage.content);
    expect(fixture.componentInstance['totalElements']()).toBe(1);
  });
});
