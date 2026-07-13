import { HttpErrorResponse, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { Observable, delay, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { createMockAccessToken } from './jwt.mock';
import {
  mockAccounts,
  mockAuditLogs,
  mockClients,
  mockCompanySettings,
  mockInvoices,
  mockJournalEntries,
  mockPayments,
  mockProducts,
  mockQuotes,
  mockServiceItems,
  mockSuppliers,
  mockUsers,
  nextClientId,
  nextInvoiceId,
  nextPaymentId,
  nextProductId,
  nextQuoteId,
  nextServiceItemId,
  nextSupplierId,
  nextUserId,
} from './mock-data';
import { paginateMock } from './mock-pagination.helper';
import { UserRole, Permission, ROLE_PERMISSIONS } from '../constants/roles.constant';
import { IUser } from '../models/user.model';
import { IClient } from '../../features/clients/models/client.model';
import { IInvoice, InvoiceStatus } from '../../features/invoices/models/invoice.model';
import { IQuote, QuoteStatus } from '../../features/quotes/models/quote.model';
import { IPayment, PaymentStatus } from '../../features/payments/models/payment.model';
import { ISupplier } from '../../features/suppliers/models/supplier.model';
import { IProduct } from '../../features/products/models/product.model';
import { IServiceItem } from '../../features/services/models/service-item.model';
import { ITrialBalanceLine } from '../../features/accounting/models/trial-balance.model';
import {
  IAgingReceivableLine,
  IQuoteFunnelLine,
  ISalesByClientLine,
} from '../../features/reports/models/report.model';
import { computeLineItemTotals } from '../../shared/helpers/line-item-calculation.helper';
import { daysBetween, isOverdue } from '../helpers/date.helper';
import { InvoiceLineDto } from '../../features/invoices/models/invoice.dto';
import { QuoteLineDto } from '../../features/quotes/models/quote.dto';
import { PaymentCreateDto } from '../../features/payments/models/payment.dto';

const MOCK_LATENCY_MS = 350;
const DEMO_USER_ID = 'demo-admin';

/**
 * Simule un backend Spring Boot en mémoire pour permettre d'explorer
 * l'application sans API réelle (mode démo). Actif uniquement lorsque
 * `environment.useMockApi` est vrai — voir `app.config.ts`.
 *
 * N'accepte pas de véritables règles métier (validation serveur, sécurité
 * réelle des mots de passe, etc.) : à retirer purement et simplement une
 * fois le backend Spring Boot disponible, en repassant `useMockApi` à `false`.
 */
export const mockApiInterceptor: HttpInterceptorFn = (req, next) => {
  if (!environment.useMockApi || !req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }

  const path = req.url.slice(environment.apiUrl.length).replace(/^\/+/, '');

  if (path === 'auth/login' && req.method === 'POST') {
    return respond(
      buildLoginResponse((req.body as { email: string })?.email ?? 'demo@facturation-pme.sn'),
    );
  }

  if (path === 'auth/refresh' && req.method === 'POST') {
    return respond(buildLoginResponse('demo@facturation-pme.sn'));
  }

  if (path === 'dashboard/kpis' && req.method === 'GET') {
    return respond({
      revenue: 9_665_000,
      revenueTrendPercent: 12,
      invoicesCount: mockInvoices.length,
      invoicesTrendPercent: 8,
      paymentsReceived: 5_150_000,
      receivables: 3_420_000,
      payables: 1_180_000,
      activeClients: mockClients.filter((c) => c.status === 'ACTIVE').length,
      productsSold: 214,
    });
  }

  if (path === 'dashboard/revenue-chart/status' && req.method === 'GET') {
    const counts = new Map<string, number>();
    for (const invoice of mockInvoices) {
      counts.set(invoice.status, (counts.get(invoice.status) ?? 0) + 1);
    }
    return respond(Array.from(counts.entries()).map(([status, count]) => ({ status, count })));
  }

  if (path === 'dashboard/revenue-chart' && req.method === 'GET') {
    return respond([
      { month: 'Jan', revenue: 4_200_000, expenses: 2_100_000 },
      { month: 'Fév', revenue: 5_100_000, expenses: 2_400_000 },
      { month: 'Mar', revenue: 4_800_000, expenses: 2_600_000 },
      { month: 'Avr', revenue: 6_300_000, expenses: 2_900_000 },
      { month: 'Mai', revenue: 7_450_000, expenses: 3_100_000 },
      { month: 'Juin', revenue: 9_665_000, expenses: 3_450_000 },
    ]);
  }

  if (path === 'dashboard/recent-activity' && req.method === 'GET') {
    return respond([
      {
        id: '1',
        type: 'invoice',
        message: 'Facture FAC-2026-0005 émise pour Casamance Fruits SA',
        createdAt: minutesAgo(15),
      },
      {
        id: '2',
        type: 'payment',
        message: 'Paiement de 900 000 XOF reçu de ACME Sénégal SARL',
        createdAt: minutesAgo(90),
      },
      {
        id: '3',
        type: 'client',
        message: 'Nouveau client ajouté : Teranga Digital',
        createdAt: minutesAgo(240),
      },
      {
        id: '4',
        type: 'quote',
        message: 'Devis DEV-2026-0012 accepté par Baobab Distribution',
        createdAt: minutesAgo(1440),
      },
    ]);
  }

  if (path === 'clients' && req.method === 'GET') {
    return respond(
      paginateMock(mockClients as unknown as Record<string, unknown>[], req.params, [
        'name',
        'email',
        'phone',
        'taxId',
      ]),
    );
  }

  if (path === 'clients' && req.method === 'POST') {
    const dto = req.body as Omit<
      IClient,
      'id' | 'status' | 'totalInvoiced' | 'createdAt' | 'updatedAt'
    >;
    const now = new Date().toISOString();
    const client: IClient = {
      ...dto,
      id: nextClientId(),
      status: 'ACTIVE' as IClient['status'],
      totalInvoiced: 0,
      createdAt: now,
      updatedAt: now,
    };
    mockClients.push(client);
    return respond(client, 201);
  }

  const clientMatch = path.match(/^clients\/([^/]+)$/);
  if (clientMatch) {
    const client = mockClients.find((c) => c.id === clientMatch[1]);
    if (!client) return notFound(`Client ${clientMatch[1]} introuvable`);

    if (req.method === 'GET') return respond(client);
    if (req.method === 'PUT') {
      Object.assign(client, req.body, { updatedAt: new Date().toISOString() });
      return respond(client);
    }
    if (req.method === 'DELETE') {
      mockClients.splice(mockClients.indexOf(client), 1);
      return respond(null);
    }
  }

  if (path === 'invoices' && req.method === 'GET') {
    return respond(
      paginateMock(mockInvoices as unknown as Record<string, unknown>[], req.params, [
        'number',
        'clientName',
      ]),
    );
  }

  if (path === 'invoices' && req.method === 'POST') {
    const invoice = createInvoiceFromDto(nextInvoiceId(), req.body);
    mockInvoices.push(invoice);
    return respond(invoice, 201);
  }

  const invoiceActionMatch = path.match(/^invoices\/([^/]+)\/(validate|send)$/);
  if (invoiceActionMatch && req.method === 'POST') {
    const invoice = mockInvoices.find((i) => i.id === invoiceActionMatch[1]);
    if (!invoice) return notFound(`Facture ${invoiceActionMatch[1]} introuvable`);
    invoice.status = invoiceActionMatch[2] === 'validate' ? InvoiceStatus.SENT : InvoiceStatus.SENT;
    invoice.updatedAt = new Date().toISOString();
    return respond(invoice);
  }

  const invoiceMatch = path.match(/^invoices\/([^/]+)$/);
  if (invoiceMatch) {
    const invoice = mockInvoices.find((i) => i.id === invoiceMatch[1]);
    if (!invoice) return notFound(`Facture ${invoiceMatch[1]} introuvable`);

    if (req.method === 'GET') return respond(invoice);
    if (req.method === 'PUT') {
      const updated = createInvoiceFromDto(invoice.id, req.body, invoice.number);
      Object.assign(invoice, updated, { updatedAt: new Date().toISOString() });
      return respond(invoice);
    }
    if (req.method === 'DELETE') {
      mockInvoices.splice(mockInvoices.indexOf(invoice), 1);
      return respond(null);
    }
  }

  if (path === 'quotes' && req.method === 'GET') {
    return respond(
      paginateMock(mockQuotes as unknown as Record<string, unknown>[], req.params, [
        'number',
        'clientName',
      ]),
    );
  }

  if (path === 'quotes' && req.method === 'POST') {
    const quote = createQuoteFromDto(nextQuoteId(), req.body);
    mockQuotes.push(quote);
    return respond(quote, 201);
  }

  const quoteActionMatch = path.match(/^quotes\/([^/]+)\/(send|accept|reject|convert-to-invoice)$/);
  if (quoteActionMatch && req.method === 'POST') {
    const quote = mockQuotes.find((q) => q.id === quoteActionMatch[1]);
    if (!quote) return notFound(`Devis ${quoteActionMatch[1]} introuvable`);

    const action = quoteActionMatch[2];
    if (action === 'convert-to-invoice') {
      quote.status = QuoteStatus.CONVERTED;
      quote.updatedAt = new Date().toISOString();
      const invoice = createInvoiceFromDto(nextInvoiceId(), {
        clientId: quote.clientId,
        issueDate: new Date().toISOString().split('T')[0],
        dueDate: quote.validUntil,
        lines: quote.lines,
        notes: quote.notes,
      });
      mockInvoices.push(invoice);
      quote.convertedInvoiceId = invoice.id;
      return respond(invoice, 201);
    }

    const statusByAction: Record<string, QuoteStatus> = {
      send: QuoteStatus.SENT,
      accept: QuoteStatus.ACCEPTED,
      reject: QuoteStatus.REJECTED,
    };
    quote.status = statusByAction[action];
    quote.updatedAt = new Date().toISOString();
    return respond(quote);
  }

  const quoteMatch = path.match(/^quotes\/([^/]+)$/);
  if (quoteMatch) {
    const quote = mockQuotes.find((q) => q.id === quoteMatch[1]);
    if (!quote) return notFound(`Devis ${quoteMatch[1]} introuvable`);

    if (req.method === 'GET') return respond(quote);
    if (req.method === 'PUT') {
      const updated = createQuoteFromDto(quote.id, req.body, quote.number);
      Object.assign(quote, updated, { updatedAt: new Date().toISOString() });
      return respond(quote);
    }
    if (req.method === 'DELETE') {
      mockQuotes.splice(mockQuotes.indexOf(quote), 1);
      return respond(null);
    }
  }

  if (path === 'payments' && req.method === 'GET') {
    return respond(
      paginateMock(mockPayments as unknown as Record<string, unknown>[], req.params, [
        'reference',
        'invoiceNumber',
        'clientName',
      ]),
    );
  }

  if (path === 'payments' && req.method === 'POST') {
    const dto = req.body as PaymentCreateDto;
    const invoice = mockInvoices.find((i) => i.id === dto.invoiceId);
    const now = new Date().toISOString();
    const payment: IPayment = {
      id: nextPaymentId(),
      reference: `PAY-2026-${String(mockPayments.length + 1).padStart(4, '0')}`,
      invoiceId: dto.invoiceId,
      invoiceNumber: invoice?.number ?? 'Facture inconnue',
      clientId: invoice?.clientId ?? '',
      clientName: invoice?.clientName ?? 'Client inconnu',
      amount: dto.amount,
      method: dto.method,
      status: PaymentStatus.COMPLETED,
      paidAt: dto.paidAt,
      notes: dto.notes,
      createdAt: now,
      updatedAt: now,
    };
    mockPayments.push(payment);

    if (invoice) {
      invoice.status =
        dto.amount >= invoice.totalAmount ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID;
      invoice.updatedAt = now;
    }

    return respond(payment, 201);
  }

  const paymentRefundMatch = path.match(/^payments\/([^/]+)\/refund$/);
  if (paymentRefundMatch && req.method === 'POST') {
    const payment = mockPayments.find((p) => p.id === paymentRefundMatch[1]);
    if (!payment) return notFound(`Paiement ${paymentRefundMatch[1]} introuvable`);
    payment.status = PaymentStatus.REFUNDED;
    payment.updatedAt = new Date().toISOString();
    return respond(payment);
  }

  const paymentMatch = path.match(/^payments\/([^/]+)$/);
  if (paymentMatch) {
    const payment = mockPayments.find((p) => p.id === paymentMatch[1]);
    if (!payment) return notFound(`Paiement ${paymentMatch[1]} introuvable`);

    if (req.method === 'GET') return respond(payment);
    if (req.method === 'DELETE') {
      mockPayments.splice(mockPayments.indexOf(payment), 1);
      return respond(null);
    }
  }

  if (path === 'suppliers' && req.method === 'GET') {
    return respond(
      paginateMock(mockSuppliers as unknown as Record<string, unknown>[], req.params, [
        'name',
        'email',
        'phone',
        'taxId',
      ]),
    );
  }

  if (path === 'suppliers' && req.method === 'POST') {
    const dto = req.body as Omit<
      ISupplier,
      'id' | 'status' | 'totalPurchased' | 'createdAt' | 'updatedAt'
    >;
    const now = new Date().toISOString();
    const supplier: ISupplier = {
      ...dto,
      id: nextSupplierId(),
      status: 'ACTIVE' as ISupplier['status'],
      totalPurchased: 0,
      createdAt: now,
      updatedAt: now,
    };
    mockSuppliers.push(supplier);
    return respond(supplier, 201);
  }

  const supplierMatch = path.match(/^suppliers\/([^/]+)$/);
  if (supplierMatch) {
    const supplier = mockSuppliers.find((s) => s.id === supplierMatch[1]);
    if (!supplier) return notFound(`Fournisseur ${supplierMatch[1]} introuvable`);

    if (req.method === 'GET') return respond(supplier);
    if (req.method === 'PUT') {
      Object.assign(supplier, req.body, { updatedAt: new Date().toISOString() });
      return respond(supplier);
    }
    if (req.method === 'DELETE') {
      mockSuppliers.splice(mockSuppliers.indexOf(supplier), 1);
      return respond(null);
    }
  }

  if (path === 'products' && req.method === 'GET') {
    return respond(
      paginateMock(mockProducts as unknown as Record<string, unknown>[], req.params, [
        'name',
        'sku',
        'category',
      ]),
    );
  }

  if (path === 'products' && req.method === 'POST') {
    const dto = req.body as Omit<IProduct, 'id' | 'status' | 'createdAt' | 'updatedAt'>;
    const now = new Date().toISOString();
    const product: IProduct = {
      ...dto,
      id: nextProductId(),
      status: 'ACTIVE' as IProduct['status'],
      createdAt: now,
      updatedAt: now,
    };
    mockProducts.push(product);
    return respond(product, 201);
  }

  const productMatch = path.match(/^products\/([^/]+)$/);
  if (productMatch) {
    const product = mockProducts.find((p) => p.id === productMatch[1]);
    if (!product) return notFound(`Produit ${productMatch[1]} introuvable`);

    if (req.method === 'GET') return respond(product);
    if (req.method === 'PUT') {
      Object.assign(product, req.body, { updatedAt: new Date().toISOString() });
      return respond(product);
    }
    if (req.method === 'DELETE') {
      mockProducts.splice(mockProducts.indexOf(product), 1);
      return respond(null);
    }
  }

  if (path === 'services' && req.method === 'GET') {
    return respond(
      paginateMock(mockServiceItems as unknown as Record<string, unknown>[], req.params, [
        'name',
        'code',
        'category',
      ]),
    );
  }

  if (path === 'services' && req.method === 'POST') {
    const dto = req.body as Omit<IServiceItem, 'id' | 'status' | 'createdAt' | 'updatedAt'>;
    const now = new Date().toISOString();
    const serviceItem: IServiceItem = {
      ...dto,
      id: nextServiceItemId(),
      status: 'ACTIVE' as IServiceItem['status'],
      createdAt: now,
      updatedAt: now,
    };
    mockServiceItems.push(serviceItem);
    return respond(serviceItem, 201);
  }

  const serviceItemMatch = path.match(/^services\/([^/]+)$/);
  if (serviceItemMatch) {
    const serviceItem = mockServiceItems.find((s) => s.id === serviceItemMatch[1]);
    if (!serviceItem) return notFound(`Prestation ${serviceItemMatch[1]} introuvable`);

    if (req.method === 'GET') return respond(serviceItem);
    if (req.method === 'PUT') {
      Object.assign(serviceItem, req.body, { updatedAt: new Date().toISOString() });
      return respond(serviceItem);
    }
    if (req.method === 'DELETE') {
      mockServiceItems.splice(mockServiceItems.indexOf(serviceItem), 1);
      return respond(null);
    }
  }

  if (path === 'accounting/journal/accounts' && req.method === 'GET') {
    return respond(mockAccounts);
  }

  if (path === 'accounting/journal' && req.method === 'GET') {
    const filtered = filterByDateRange(
      mockJournalEntries,
      req.params.get('startDate'),
      req.params.get('endDate'),
    );
    return respond(
      paginateMock(filtered as unknown as Record<string, unknown>[], req.params, [
        'reference',
        'label',
        'accountName',
      ]),
    );
  }

  const ledgerMatch = path.match(/^accounting\/ledger\/([^/]+)$/);
  if (ledgerMatch && req.method === 'GET') {
    const accountEntries = mockJournalEntries.filter(
      (entry) => entry.accountCode === ledgerMatch[1],
    );
    const filtered = filterByDateRange(
      accountEntries,
      req.params.get('startDate'),
      req.params.get('endDate'),
    );
    return respond(
      paginateMock(filtered as unknown as Record<string, unknown>[], req.params, [
        'reference',
        'label',
      ]),
    );
  }

  if (path === 'accounting/trial-balance' && req.method === 'GET') {
    return respond(buildTrialBalance());
  }

  if (path === 'accounting/cashbook' && req.method === 'GET') {
    const treasuryEntries = mockJournalEntries.filter(
      (entry) => entry.accountCode === '512' || entry.accountCode === '531',
    );
    const filtered = filterByDateRange(
      treasuryEntries,
      req.params.get('startDate'),
      req.params.get('endDate'),
    );
    return respond(
      paginateMock(filtered as unknown as Record<string, unknown>[], req.params, [
        'reference',
        'label',
      ]),
    );
  }

  if (path === 'users' && req.method === 'GET') {
    return respond(
      paginateMock(mockUsers as unknown as Record<string, unknown>[], req.params, [
        'firstName',
        'lastName',
        'email',
      ]),
    );
  }

  if (path === 'users' && req.method === 'POST') {
    const dto = req.body as { firstName: string; lastName: string; email: string; role: UserRole };
    const now = new Date().toISOString();
    const user: IUser = {
      id: nextUserId(),
      firstName: dto.firstName,
      lastName: dto.lastName,
      email: dto.email,
      role: dto.role,
      permissions: [...ROLE_PERMISSIONS[dto.role]],
      isActive: true,
      createdAt: now,
    };
    mockUsers.push(user);
    return respond(user, 201);
  }

  const userResetPasswordMatch = path.match(/^users\/([^/]+)\/reset-password$/);
  if (userResetPasswordMatch && req.method === 'POST') {
    const user = mockUsers.find((u) => u.id === userResetPasswordMatch[1]);
    if (!user) return notFound(`Utilisateur ${userResetPasswordMatch[1]} introuvable`);
    return respond(null);
  }

  const userMatch = path.match(/^users\/([^/]+)$/);
  if (userMatch) {
    const user = mockUsers.find((u) => u.id === userMatch[1]);
    if (!user) return notFound(`Utilisateur ${userMatch[1]} introuvable`);

    if (req.method === 'GET') return respond(user);
    if (req.method === 'PUT') {
      const dto = req.body as {
        firstName: string;
        lastName: string;
        email: string;
        role: UserRole;
        isActive: boolean;
      };
      user.firstName = dto.firstName;
      user.lastName = dto.lastName;
      user.email = dto.email;
      user.role = dto.role;
      user.isActive = dto.isActive;
      user.permissions = [...ROLE_PERMISSIONS[dto.role]];
      return respond(user);
    }
    if (req.method === 'DELETE') {
      mockUsers.splice(mockUsers.indexOf(user), 1);
      return respond(null);
    }
  }

  if (path === 'profile' && req.method === 'PUT') {
    const dto = req.body as { firstName: string; lastName: string; email: string };
    const updatedUser: IUser = {
      id: DEMO_USER_ID,
      firstName: dto.firstName,
      lastName: dto.lastName,
      email: dto.email,
      role: UserRole.ADMIN,
      permissions: Object.values(Permission),
      isActive: true,
      createdAt: '2025-01-01T00:00:00.000Z',
    };
    return respond(updatedUser);
  }

  if (path === 'profile/change-password' && req.method === 'POST') {
    return respond(null);
  }

  if (path === 'settings/company' && req.method === 'GET') {
    return respond(mockCompanySettings);
  }

  if (path === 'settings/company' && req.method === 'PUT') {
    Object.assign(mockCompanySettings, req.body);
    return respond(mockCompanySettings);
  }

  if (path === 'reports/sales-by-client' && req.method === 'GET') {
    return respond(buildSalesByClient());
  }

  if (path === 'reports/aging-receivables' && req.method === 'GET') {
    return respond(buildAgingReceivables());
  }

  if (path === 'reports/quote-funnel' && req.method === 'GET') {
    return respond(buildQuoteFunnel());
  }

  if (path === 'audit-logs' && req.method === 'GET') {
    return respond(
      paginateMock(mockAuditLogs as unknown as Record<string, unknown>[], req.params, [
        'userName',
        'entityType',
        'entityLabel',
        'details',
      ]),
    );
  }

  return next(req);
};

function buildLoginResponse(email: string) {
  const user: IUser = {
    id: DEMO_USER_ID,
    firstName: 'Admin',
    lastName: 'Démo',
    email,
    role: UserRole.ADMIN,
    permissions: Object.values(Permission),
    isActive: true,
    createdAt: '2025-01-01T00:00:00.000Z',
  };

  const accessToken = createMockAccessToken({
    sub: user.id,
    email: user.email,
    role: user.role,
    permissions: user.permissions,
  });

  return { accessToken, refreshToken: 'mock-refresh-token', expiresIn: 3600, user };
}

function createInvoiceFromDto(id: string, body: unknown, existingNumber?: string): IInvoice {
  const dto = body as {
    clientId: string;
    issueDate: string;
    dueDate: string;
    lines: InvoiceLineDto[];
    notes?: string;
  };
  const client = mockClients.find((c) => c.id === dto.clientId);
  const totals = computeLineItemTotals(dto.lines);
  const now = new Date().toISOString();

  return {
    id,
    number: existingNumber ?? `FAC-2026-${String(mockInvoices.length + 1).padStart(4, '0')}`,
    clientId: dto.clientId,
    clientName: client?.name ?? 'Client inconnu',
    issueDate: dto.issueDate,
    dueDate: dto.dueDate,
    lines: dto.lines.map((line, index) => ({
      id: `${id}-L${index + 1}`,
      ...line,
      lineTotal: Math.round(line.quantity * line.unitPrice * (1 + line.taxRate / 100)),
    })),
    amountExclTax: totals.amountExclTax,
    taxAmount: totals.taxAmount,
    totalAmount: totals.totalAmount,
    status: InvoiceStatus.DRAFT,
    notes: dto.notes,
    createdAt: now,
    updatedAt: now,
  };
}

function createQuoteFromDto(id: string, body: unknown, existingNumber?: string): IQuote {
  const dto = body as {
    clientId: string;
    issueDate: string;
    validUntil: string;
    lines: QuoteLineDto[];
    notes?: string;
  };
  const client = mockClients.find((c) => c.id === dto.clientId);
  const totals = computeLineItemTotals(dto.lines);
  const now = new Date().toISOString();

  return {
    id,
    number: existingNumber ?? `DEV-2026-${String(mockQuotes.length + 1).padStart(4, '0')}`,
    clientId: dto.clientId,
    clientName: client?.name ?? 'Client inconnu',
    issueDate: dto.issueDate,
    validUntil: dto.validUntil,
    lines: dto.lines.map((line, index) => ({
      id: `${id}-L${index + 1}`,
      ...line,
      lineTotal: Math.round(line.quantity * line.unitPrice * (1 + line.taxRate / 100)),
    })),
    amountExclTax: totals.amountExclTax,
    taxAmount: totals.taxAmount,
    totalAmount: totals.totalAmount,
    status: QuoteStatus.DRAFT,
    notes: dto.notes,
    createdAt: now,
    updatedAt: now,
  };
}

function respond<T>(body: T, status = 200): Observable<HttpResponse<T>> {
  return of(new HttpResponse({ status, body })).pipe(delay(MOCK_LATENCY_MS));
}

function notFound(message: string): Observable<never> {
  return throwError(
    () =>
      new HttpErrorResponse({
        status: 404,
        error: { message, code: 'NOT_FOUND', timestamp: new Date().toISOString() },
      }),
  ).pipe(delay(MOCK_LATENCY_MS));
}

function minutesAgo(minutes: number): string {
  return new Date(Date.now() - minutes * 60_000).toISOString();
}

function filterByDateRange<T extends { date: string }>(
  entries: T[],
  startDate: string | null,
  endDate: string | null,
): T[] {
  return entries.filter((entry) => {
    if (startDate && entry.date < startDate) return false;
    if (endDate && entry.date > endDate) return false;
    return true;
  });
}

function buildTrialBalance(): ITrialBalanceLine[] {
  const byAccount = new Map<string, ITrialBalanceLine>();

  for (const entry of mockJournalEntries) {
    const existing = byAccount.get(entry.accountCode) ?? {
      accountCode: entry.accountCode,
      accountName: entry.accountName,
      totalDebit: 0,
      totalCredit: 0,
      balance: 0,
    };
    existing.totalDebit += entry.debit;
    existing.totalCredit += entry.credit;
    existing.balance = existing.totalDebit - existing.totalCredit;
    byAccount.set(entry.accountCode, existing);
  }

  return Array.from(byAccount.values()).sort((a, b) => a.accountCode.localeCompare(b.accountCode));
}

function buildSalesByClient(): ISalesByClientLine[] {
  const byClient = new Map<string, ISalesByClientLine>();

  for (const invoice of mockInvoices) {
    if (invoice.status === InvoiceStatus.DRAFT || invoice.status === InvoiceStatus.CANCELLED) {
      continue;
    }
    const existing = byClient.get(invoice.clientId) ?? {
      clientId: invoice.clientId,
      clientName: invoice.clientName,
      invoiceCount: 0,
      amountExclTax: 0,
      taxAmount: 0,
      totalAmount: 0,
    };
    existing.invoiceCount += 1;
    existing.amountExclTax += invoice.amountExclTax;
    existing.taxAmount += invoice.taxAmount;
    existing.totalAmount += invoice.totalAmount;
    byClient.set(invoice.clientId, existing);
  }

  return Array.from(byClient.values()).sort((a, b) => b.totalAmount - a.totalAmount);
}

function buildAgingReceivables(): IAgingReceivableLine[] {
  return mockInvoices
    .filter((invoice) => invoice.status === InvoiceStatus.OVERDUE && isOverdue(invoice.dueDate))
    .map((invoice) => ({
      invoiceId: invoice.id,
      invoiceNumber: invoice.number,
      clientName: invoice.clientName,
      dueDate: invoice.dueDate,
      daysOverdue: daysBetween(invoice.dueDate, new Date()),
      amountDue: invoice.totalAmount,
    }))
    .sort((a, b) => b.daysOverdue - a.daysOverdue);
}

function buildQuoteFunnel(): IQuoteFunnelLine[] {
  const byStatus = new Map<string, IQuoteFunnelLine>();

  for (const quote of mockQuotes) {
    const existing = byStatus.get(quote.status) ?? {
      status: quote.status,
      count: 0,
      totalAmount: 0,
    };
    existing.count += 1;
    existing.totalAmount += quote.totalAmount;
    byStatus.set(quote.status, existing);
  }

  return Array.from(byStatus.values());
}
