import { ClientStatus, IClient } from '../../features/clients/models/client.model';
import { IInvoice, InvoiceStatus } from '../../features/invoices/models/invoice.model';
import { IQuote, QuoteStatus } from '../../features/quotes/models/quote.model';
import {
  IPayment,
  PaymentMethod,
  PaymentStatus,
} from '../../features/payments/models/payment.model';
import { ISupplier, SupplierStatus } from '../../features/suppliers/models/supplier.model';
import { IProduct, ProductStatus, ProductUnit } from '../../features/products/models/product.model';
import {
  IServiceItem,
  ServiceItemStatus,
  ServiceItemUnit,
} from '../../features/services/models/service-item.model';
import { AccountType, IAccount } from '../../features/accounting/models/account.model';
import { IJournalEntry, JournalSource } from '../../features/accounting/models/journal-entry.model';
import { IUser } from '../models/user.model';
import { ROLE_PERMISSIONS, UserRole } from '../constants/roles.constant';
import { ICompanySettings } from '../../features/settings/models/company-settings.model';
import { computeLineItemTotals } from '../../shared/helpers/line-item-calculation.helper';
import { AuditAction, IAuditLog } from '../../features/audit/models/audit-log.model';

/**
 * Jeux de données en mémoire utilisés par `mock-api.interceptor.ts` en mode
 * démo. Mutés directement par les opérations CRUD simulées (create/update/
 * delete) pendant la durée de la session navigateur — aucune persistance.
 */

let clientIdCounter = 100;
let invoiceIdCounter = 100;
let quoteIdCounter = 100;
let paymentIdCounter = 100;
let supplierIdCounter = 100;
let productIdCounter = 100;
let serviceItemIdCounter = 100;
let userIdCounter = 100;

export const mockClients: IClient[] = [
  {
    id: '1',
    name: 'ACME Sénégal SARL',
    email: 'contact@acme.sn',
    phone: '+221 77 123 45 67',
    address: 'Rue 10, Plateau, Dakar, Sénégal',
    taxId: 'NINEA1234567',
    status: ClientStatus.ACTIVE,
    totalInvoiced: 4_250_000,
    createdAt: '2025-11-02T09:00:00.000Z',
    updatedAt: '2026-06-01T09:00:00.000Z',
  },
  {
    id: '2',
    name: 'Baobab Distribution',
    email: 'compta@baobab-distrib.sn',
    phone: '+221 76 987 65 43',
    address: 'Zone Industrielle, Thiès, Sénégal',
    taxId: 'NINEA7654321',
    status: ClientStatus.ACTIVE,
    totalInvoiced: 1_875_000,
    createdAt: '2025-12-15T09:00:00.000Z',
    updatedAt: '2026-05-20T09:00:00.000Z',
  },
  {
    id: '3',
    name: 'Teranga Digital',
    email: 'billing@terangadigital.com',
    phone: '+221 70 555 44 33',
    address: 'Almadies, Dakar, Sénégal',
    taxId: 'NINEA1122334',
    status: ClientStatus.ACTIVE,
    totalInvoiced: 980_000,
    createdAt: '2026-01-10T09:00:00.000Z',
    updatedAt: '2026-06-10T09:00:00.000Z',
  },
  {
    id: '4',
    name: 'Saloum Agro Export',
    email: 'finance@saloumagro.sn',
    phone: '+221 78 222 11 00',
    address: 'Kaolack, Sénégal',
    taxId: 'NINEA9988776',
    status: ClientStatus.INACTIVE,
    totalInvoiced: 320_000,
    createdAt: '2025-09-05T09:00:00.000Z',
    updatedAt: '2026-02-18T09:00:00.000Z',
  },
  {
    id: '5',
    name: 'Casamance Fruits SA',
    email: 'contact@casamancefruits.sn',
    phone: '+221 77 444 33 22',
    address: 'Ziguinchor, Sénégal',
    taxId: 'NINEA5566778',
    status: ClientStatus.ACTIVE,
    totalInvoiced: 2_640_000,
    createdAt: '2026-02-01T09:00:00.000Z',
    updatedAt: '2026-06-25T09:00:00.000Z',
  },
];

function buildInvoice(
  id: string,
  number: string,
  client: IClient,
  issueDate: string,
  dueDate: string,
  status: InvoiceStatus,
  lines: { description: string; quantity: number; unitPrice: number; taxRate: number }[],
): IInvoice {
  const totals = computeLineItemTotals(lines);
  return {
    id,
    number,
    clientId: client.id,
    clientName: client.name,
    issueDate,
    dueDate,
    lines: lines.map((line, index) => ({
      id: `${id}-L${index + 1}`,
      ...line,
      lineTotal: Math.round(line.quantity * line.unitPrice * (1 + line.taxRate / 100)),
    })),
    amountExclTax: totals.amountExclTax,
    taxAmount: totals.taxAmount,
    totalAmount: totals.totalAmount,
    status,
    createdAt: issueDate,
    updatedAt: issueDate,
  };
}

export const mockInvoices: IInvoice[] = [
  buildInvoice(
    '1',
    'FAC-2026-0001',
    mockClients[0],
    '2026-05-01',
    '2026-05-31',
    InvoiceStatus.PAID,
    [
      {
        description: 'Développement application web',
        quantity: 1,
        unitPrice: 2_500_000,
        taxRate: 18,
      },
    ],
  ),
  buildInvoice(
    '2',
    'FAC-2026-0002',
    mockClients[1],
    '2026-05-10',
    '2026-06-10',
    InvoiceStatus.SENT,
    [
      { description: 'Prestation de conseil', quantity: 5, unitPrice: 150_000, taxRate: 18 },
      { description: 'Formation utilisateurs', quantity: 2, unitPrice: 100_000, taxRate: 18 },
    ],
  ),
  buildInvoice(
    '3',
    'FAC-2026-0003',
    mockClients[2],
    '2026-06-01',
    '2026-06-30',
    InvoiceStatus.DRAFT,
    [{ description: 'Maintenance mensuelle', quantity: 1, unitPrice: 300_000, taxRate: 18 }],
  ),
  buildInvoice(
    '4',
    'FAC-2026-0004',
    mockClients[0],
    '2026-04-01',
    '2026-04-30',
    InvoiceStatus.OVERDUE,
    [{ description: 'Licence logicielle annuelle', quantity: 1, unitPrice: 900_000, taxRate: 18 }],
  ),
  buildInvoice(
    '5',
    'FAC-2026-0005',
    mockClients[4],
    '2026-06-15',
    '2026-07-15',
    InvoiceStatus.PARTIALLY_PAID,
    [{ description: 'Export de conteneur - fruits', quantity: 3, unitPrice: 400_000, taxRate: 0 }],
  ),
  buildInvoice(
    '6',
    'FAC-2026-0006',
    mockClients[3],
    '2026-03-01',
    '2026-03-31',
    InvoiceStatus.CANCELLED,
    [{ description: 'Prestation annulée', quantity: 1, unitPrice: 150_000, taxRate: 18 }],
  ),
];

function buildQuote(
  id: string,
  number: string,
  client: IClient,
  issueDate: string,
  validUntil: string,
  status: QuoteStatus,
  lines: { description: string; quantity: number; unitPrice: number; taxRate: number }[],
): IQuote {
  const totals = computeLineItemTotals(lines);
  return {
    id,
    number,
    clientId: client.id,
    clientName: client.name,
    issueDate,
    validUntil,
    lines: lines.map((line, index) => ({
      id: `${id}-L${index + 1}`,
      ...line,
      lineTotal: Math.round(line.quantity * line.unitPrice * (1 + line.taxRate / 100)),
    })),
    amountExclTax: totals.amountExclTax,
    taxAmount: totals.taxAmount,
    totalAmount: totals.totalAmount,
    status,
    createdAt: issueDate,
    updatedAt: issueDate,
  };
}

export const mockQuotes: IQuote[] = [
  buildQuote('1', 'DEV-2026-0001', mockClients[2], '2026-06-20', '2026-07-20', QuoteStatus.SENT, [
    { description: 'Refonte site vitrine', quantity: 1, unitPrice: 1_200_000, taxRate: 18 },
  ]),
  buildQuote(
    '2',
    'DEV-2026-0002',
    mockClients[1],
    '2026-06-05',
    '2026-07-05',
    QuoteStatus.ACCEPTED,
    [{ description: 'Audit logistique', quantity: 3, unitPrice: 200_000, taxRate: 18 }],
  ),
  buildQuote(
    '3',
    'DEV-2026-0003',
    mockClients[4],
    '2026-05-15',
    '2026-06-15',
    QuoteStatus.CONVERTED,
    [{ description: 'Étude de faisabilité export', quantity: 1, unitPrice: 500_000, taxRate: 0 }],
  ),
  buildQuote(
    '4',
    'DEV-2026-0004',
    mockClients[0],
    '2026-04-10',
    '2026-05-10',
    QuoteStatus.EXPIRED,
    [{ description: 'Intégration ERP', quantity: 1, unitPrice: 3_000_000, taxRate: 18 }],
  ),
  buildQuote('5', 'DEV-2026-0005', mockClients[3], '2026-06-25', '2026-07-25', QuoteStatus.DRAFT, [
    { description: 'Campagne marketing digitale', quantity: 1, unitPrice: 450_000, taxRate: 18 },
  ]),
];

function buildPayment(
  id: string,
  reference: string,
  invoice: IInvoice,
  amount: number,
  method: PaymentMethod,
  status: PaymentStatus,
  paidAt: string,
): IPayment {
  return {
    id,
    reference,
    invoiceId: invoice.id,
    invoiceNumber: invoice.number,
    clientId: invoice.clientId,
    clientName: invoice.clientName,
    amount,
    method,
    status,
    paidAt,
    createdAt: paidAt,
    updatedAt: paidAt,
  };
}

export const mockPayments: IPayment[] = [
  buildPayment(
    '1',
    'PAY-2026-0001',
    mockInvoices[0],
    2_950_000,
    PaymentMethod.BANK_TRANSFER,
    PaymentStatus.COMPLETED,
    '2026-05-20',
  ),
  buildPayment(
    '2',
    'PAY-2026-0002',
    mockInvoices[4],
    600_000,
    PaymentMethod.MOBILE_MONEY,
    PaymentStatus.COMPLETED,
    '2026-06-20',
  ),
  buildPayment(
    '3',
    'PAY-2026-0003',
    mockInvoices[1],
    400_000,
    PaymentMethod.CASH,
    PaymentStatus.PENDING,
    '2026-06-12',
  ),
  buildPayment(
    '4',
    'PAY-2026-0004',
    mockInvoices[5],
    177_000,
    PaymentMethod.CHECK,
    PaymentStatus.REFUNDED,
    '2026-03-05',
  ),
];

export function nextClientId(): string {
  clientIdCounter += 1;
  return String(clientIdCounter);
}

export function nextInvoiceId(): string {
  invoiceIdCounter += 1;
  return String(invoiceIdCounter);
}

export function nextQuoteId(): string {
  quoteIdCounter += 1;
  return String(quoteIdCounter);
}

export function nextPaymentId(): string {
  paymentIdCounter += 1;
  return String(paymentIdCounter);
}

export const mockSuppliers: ISupplier[] = [
  {
    id: '1',
    name: 'Sahel Fournitures SARL',
    email: 'contact@sahel-fournitures.sn',
    phone: '+221 33 821 45 67',
    address: 'Zone Industrielle, Dakar, Sénégal',
    taxId: 'NINEA2233445',
    status: SupplierStatus.ACTIVE,
    totalPurchased: 3_100_000,
    createdAt: '2025-10-01T09:00:00.000Z',
    updatedAt: '2026-06-01T09:00:00.000Z',
  },
  {
    id: '2',
    name: 'Dakar Papeterie Pro',
    email: 'ventes@dakarpapeterie.sn',
    phone: '+221 76 334 21 09',
    address: 'Médina, Dakar, Sénégal',
    taxId: 'NINEA3344556',
    status: SupplierStatus.ACTIVE,
    totalPurchased: 540_000,
    createdAt: '2025-11-20T09:00:00.000Z',
    updatedAt: '2026-05-15T09:00:00.000Z',
  },
  {
    id: '3',
    name: 'Thiès Matériaux Export',
    email: 'contact@thiesmateriaux.sn',
    phone: '+221 77 665 44 22',
    address: 'Thiès, Sénégal',
    taxId: 'NINEA4455667',
    status: SupplierStatus.INACTIVE,
    totalPurchased: 1_250_000,
    createdAt: '2025-08-12T09:00:00.000Z',
    updatedAt: '2026-01-30T09:00:00.000Z',
  },
  {
    id: '4',
    name: 'Sénégal IT Solutions',
    email: 'support@senegalit.sn',
    phone: '+221 78 990 11 22',
    address: 'Almadies, Dakar, Sénégal',
    taxId: 'NINEA5566990',
    status: SupplierStatus.ACTIVE,
    totalPurchased: 2_400_000,
    createdAt: '2026-02-05T09:00:00.000Z',
    updatedAt: '2026-06-20T09:00:00.000Z',
  },
];

export function nextSupplierId(): string {
  supplierIdCounter += 1;
  return String(supplierIdCounter);
}

export const mockProducts: IProduct[] = [
  {
    id: '1',
    name: 'Ordinateur portable 15"',
    sku: 'PRD-0001',
    description: 'Ordinateur portable professionnel 15 pouces, 16 Go RAM, 512 Go SSD.',
    category: 'Informatique',
    unitPrice: 450_000,
    taxRate: 18,
    unit: ProductUnit.PIECE,
    stockQuantity: 12,
    status: ProductStatus.ACTIVE,
    createdAt: '2025-10-10T09:00:00.000Z',
    updatedAt: '2026-06-01T09:00:00.000Z',
  },
  {
    id: '2',
    name: 'Ramette papier A4',
    sku: 'PRD-0002',
    description: 'Ramette de 500 feuilles A4 80g.',
    category: 'Fournitures de bureau',
    unitPrice: 3_500,
    taxRate: 18,
    unit: ProductUnit.BOX,
    stockQuantity: 3,
    status: ProductStatus.ACTIVE,
    createdAt: '2025-11-05T09:00:00.000Z',
    updatedAt: '2026-05-18T09:00:00.000Z',
  },
  {
    id: '3',
    name: 'Prestation de conseil',
    sku: 'PRD-0003',
    description: "Heure de conseil en stratégie d'entreprise.",
    category: 'Services',
    unitPrice: 75_000,
    taxRate: 18,
    unit: ProductUnit.HOUR,
    stockQuantity: 0,
    status: ProductStatus.ACTIVE,
    createdAt: '2025-12-01T09:00:00.000Z',
    updatedAt: '2026-06-10T09:00:00.000Z',
  },
  {
    id: '4',
    name: 'Riz local 25kg',
    sku: 'PRD-0004',
    description: 'Sac de riz local de 25 kilogrammes.',
    category: 'Agroalimentaire',
    unitPrice: 18_000,
    taxRate: 0,
    unit: ProductUnit.KG,
    stockQuantity: 240,
    status: ProductStatus.ACTIVE,
    createdAt: '2026-01-15T09:00:00.000Z',
    updatedAt: '2026-06-22T09:00:00.000Z',
  },
  {
    id: '5',
    name: 'Ancien modèle imprimante',
    sku: 'PRD-0005',
    description: 'Imprimante laser monochrome — modèle discontinué.',
    category: 'Informatique',
    unitPrice: 95_000,
    taxRate: 18,
    unit: ProductUnit.PIECE,
    stockQuantity: 2,
    status: ProductStatus.INACTIVE,
    createdAt: '2025-06-01T09:00:00.000Z',
    updatedAt: '2026-01-05T09:00:00.000Z',
  },
];

export function nextProductId(): string {
  productIdCounter += 1;
  return String(productIdCounter);
}

export const mockServiceItems: IServiceItem[] = [
  {
    id: '1',
    name: 'Consultation stratégique',
    code: 'SRV-0001',
    description: "Séance de conseil en stratégie d'entreprise.",
    category: 'Conseil',
    unitPrice: 75_000,
    taxRate: 18,
    unit: ServiceItemUnit.HOUR,
    status: ServiceItemStatus.ACTIVE,
    createdAt: '2025-10-01T09:00:00.000Z',
    updatedAt: '2026-06-01T09:00:00.000Z',
  },
  {
    id: '2',
    name: 'Développement application web',
    code: 'SRV-0002',
    description: 'Développement sur mesure — forfait projet.',
    category: 'Informatique',
    unitPrice: 2_500_000,
    taxRate: 18,
    unit: ServiceItemUnit.FLAT_RATE,
    status: ServiceItemStatus.ACTIVE,
    createdAt: '2025-11-10T09:00:00.000Z',
    updatedAt: '2026-05-15T09:00:00.000Z',
  },
  {
    id: '3',
    name: 'Maintenance mensuelle',
    code: 'SRV-0003',
    description: 'Forfait de maintenance et support technique.',
    category: 'Informatique',
    unitPrice: 300_000,
    taxRate: 18,
    unit: ServiceItemUnit.MONTH,
    status: ServiceItemStatus.ACTIVE,
    createdAt: '2025-12-05T09:00:00.000Z',
    updatedAt: '2026-06-10T09:00:00.000Z',
  },
  {
    id: '4',
    name: 'Formation utilisateurs',
    code: 'SRV-0004',
    description: 'Journée de formation sur site.',
    category: 'Formation',
    unitPrice: 400_000,
    taxRate: 18,
    unit: ServiceItemUnit.DAY,
    status: ServiceItemStatus.INACTIVE,
    createdAt: '2025-08-20T09:00:00.000Z',
    updatedAt: '2026-01-10T09:00:00.000Z',
  },
];

export function nextServiceItemId(): string {
  serviceItemIdCounter += 1;
  return String(serviceItemIdCounter);
}

export const mockAccounts: IAccount[] = [
  { code: '411', name: 'Clients', type: AccountType.ASSET },
  { code: '512', name: 'Banque', type: AccountType.ASSET },
  { code: '531', name: 'Caisse', type: AccountType.ASSET },
  { code: '706', name: 'Prestations de services', type: AccountType.REVENUE },
  { code: '44571', name: 'TVA collectée', type: AccountType.LIABILITY },
];

/**
 * Génère les écritures comptables (partie double) à partir des factures
 * validées et des paiements complétés — pour que les vues comptables du
 * mode démo reflètent des données cohérentes avec le reste de l'application.
 */
function buildMockJournalEntries(): IJournalEntry[] {
  const entries: IJournalEntry[] = [];
  let counter = 0;
  const nextId = () => `JE-${(counter += 1)}`;

  for (const invoice of mockInvoices) {
    if (invoice.status === InvoiceStatus.DRAFT || invoice.status === InvoiceStatus.CANCELLED) {
      continue;
    }
    const label = `Facture ${invoice.number} — ${invoice.clientName}`;
    entries.push({
      id: nextId(),
      date: invoice.issueDate,
      reference: invoice.number,
      accountCode: '411',
      accountName: 'Clients',
      label,
      debit: invoice.totalAmount,
      credit: 0,
      source: JournalSource.INVOICE,
    });
    entries.push({
      id: nextId(),
      date: invoice.issueDate,
      reference: invoice.number,
      accountCode: '706',
      accountName: 'Prestations de services',
      label,
      debit: 0,
      credit: invoice.amountExclTax,
      source: JournalSource.INVOICE,
    });
    if (invoice.taxAmount > 0) {
      entries.push({
        id: nextId(),
        date: invoice.issueDate,
        reference: invoice.number,
        accountCode: '44571',
        accountName: 'TVA collectée',
        label,
        debit: 0,
        credit: invoice.taxAmount,
        source: JournalSource.INVOICE,
      });
    }
  }

  for (const payment of mockPayments) {
    if (payment.status !== PaymentStatus.COMPLETED) {
      continue;
    }
    const treasuryAccount =
      payment.method === PaymentMethod.CASH
        ? { code: '531', name: 'Caisse' }
        : { code: '512', name: 'Banque' };
    const label = `Encaissement ${payment.reference} — ${payment.clientName}`;
    entries.push({
      id: nextId(),
      date: payment.paidAt,
      reference: payment.reference,
      accountCode: treasuryAccount.code,
      accountName: treasuryAccount.name,
      label,
      debit: payment.amount,
      credit: 0,
      source: JournalSource.PAYMENT,
    });
    entries.push({
      id: nextId(),
      date: payment.paidAt,
      reference: payment.reference,
      accountCode: '411',
      accountName: 'Clients',
      label,
      debit: 0,
      credit: payment.amount,
      source: JournalSource.PAYMENT,
    });
  }

  return entries.sort((a, b) => a.date.localeCompare(b.date));
}

export const mockJournalEntries: IJournalEntry[] = buildMockJournalEntries();

export const mockUsers: IUser[] = [
  {
    id: '1',
    firstName: 'Admin',
    lastName: 'Démo',
    email: 'admin@facturation-pme.sn',
    role: UserRole.ADMIN,
    permissions: [...ROLE_PERMISSIONS[UserRole.ADMIN]],
    isActive: true,
    lastLoginAt: '2026-07-08T08:30:00.000Z',
    createdAt: '2025-01-01T00:00:00.000Z',
  },
  {
    id: '2',
    firstName: 'Awa',
    lastName: 'Diop',
    email: 'awa.diop@facturation-pme.sn',
    role: UserRole.COMPTABLE,
    permissions: [...ROLE_PERMISSIONS[UserRole.COMPTABLE]],
    isActive: true,
    lastLoginAt: '2026-07-07T17:15:00.000Z',
    createdAt: '2025-03-10T00:00:00.000Z',
  },
  {
    id: '3',
    firstName: 'Moussa',
    lastName: 'Ndiaye',
    email: 'moussa.ndiaye@facturation-pme.sn',
    role: UserRole.GESTIONNAIRE,
    permissions: [...ROLE_PERMISSIONS[UserRole.GESTIONNAIRE]],
    isActive: true,
    lastLoginAt: '2026-07-06T09:00:00.000Z',
    createdAt: '2025-05-20T00:00:00.000Z',
  },
  {
    id: '4',
    firstName: 'Fatou',
    lastName: 'Sow',
    email: 'fatou.sow@facturation-pme.sn',
    role: UserRole.CAISSIER,
    permissions: [...ROLE_PERMISSIONS[UserRole.CAISSIER]],
    isActive: true,
    lastLoginAt: '2026-07-08T07:45:00.000Z',
    createdAt: '2025-08-01T00:00:00.000Z',
  },
  {
    id: '5',
    firstName: 'Ibrahima',
    lastName: 'Fall',
    email: 'ibrahima.fall@facturation-pme.sn',
    role: UserRole.AUDITEUR,
    permissions: [...ROLE_PERMISSIONS[UserRole.AUDITEUR]],
    isActive: true,
    createdAt: '2025-11-15T00:00:00.000Z',
  },
  {
    id: '6',
    firstName: 'Cheikh',
    lastName: 'Ba',
    email: 'cheikh.ba@facturation-pme.sn',
    role: UserRole.UTILISATEUR,
    permissions: [...ROLE_PERMISSIONS[UserRole.UTILISATEUR]],
    isActive: false,
    lastLoginAt: '2026-04-12T00:00:00.000Z',
    createdAt: '2026-01-05T00:00:00.000Z',
  },
];

export function nextUserId(): string {
  userIdCounter += 1;
  return String(userIdCounter);
}

export const mockCompanySettings: ICompanySettings = {
  companyName: 'FacturationPME SARL',
  address: 'Rue 12, Plateau, Dakar, Sénégal',
  taxId: 'NINEA0011223',
  currency: 'XOF',
  defaultTaxRate: 18,
  invoicePrefix: 'FAC',
  quotePrefix: 'DEV',
};

export const mockAuditLogs: IAuditLog[] = [
  {
    id: '1',
    timestamp: '2026-07-08T08:30:00.000Z',
    userName: 'Admin Démo',
    action: AuditAction.LOGIN,
    entityType: 'Auth',
    entityLabel: 'Connexion',
  },
  {
    id: '2',
    timestamp: '2026-07-07T17:20:00.000Z',
    userName: 'Awa Diop',
    action: AuditAction.CREATE,
    entityType: 'Facture',
    entityLabel: 'FAC-2026-0005',
  },
  {
    id: '3',
    timestamp: '2026-07-07T16:05:00.000Z',
    userName: 'Awa Diop',
    action: AuditAction.VALIDATE,
    entityType: 'Écriture comptable',
    entityLabel: 'JRN-2026-0032',
  },
  {
    id: '4',
    timestamp: '2026-07-06T11:40:00.000Z',
    userName: 'Moussa Ndiaye',
    action: AuditAction.UPDATE,
    entityType: 'Client',
    entityLabel: 'ACME SARL',
    details: 'Changement de statut : ACTIF',
  },
  {
    id: '5',
    timestamp: '2026-07-06T09:10:00.000Z',
    userName: 'Fatou Sow',
    action: AuditAction.CREATE,
    entityType: 'Paiement',
    entityLabel: 'PAY-2026-0012',
  },
  {
    id: '6',
    timestamp: '2026-07-05T14:50:00.000Z',
    userName: 'Moussa Ndiaye',
    action: AuditAction.DELETE,
    entityType: 'Devis',
    entityLabel: 'DEV-2026-0008',
  },
  {
    id: '7',
    timestamp: '2026-07-05T10:00:00.000Z',
    userName: 'Ibrahima Fall',
    action: AuditAction.EXPORT,
    entityType: 'Rapport',
    entityLabel: 'Ventes par client',
  },
  {
    id: '8',
    timestamp: '2026-07-04T18:22:00.000Z',
    userName: 'Admin Démo',
    action: AuditAction.UPDATE,
    entityType: 'Utilisateur',
    entityLabel: 'Cheikh Ba',
    details: 'Compte désactivé',
  },
];
