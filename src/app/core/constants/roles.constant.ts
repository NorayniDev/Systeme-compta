/**
 * Rôles applicatifs pris en charge par le contrôle d'accès RBAC.
 * Doit rester synchronisé avec l'énumération exposée par le backend Spring Boot.
 */
export enum UserRole {
  ADMIN = 'ADMIN',
  COMPTABLE = 'COMPTABLE',
  GESTIONNAIRE = 'GESTIONNAIRE',
  CAISSIER = 'CAISSIER',
  AUDITEUR = 'AUDITEUR',
  UTILISATEUR = 'UTILISATEUR',
}

/**
 * Permissions granulaires utilisées par la directive/guard de permission.
 * Une permission représente une action métier précise (ressource:action).
 */
export enum Permission {
  INVOICE_CREATE = 'invoice:create',
  INVOICE_READ = 'invoice:read',
  INVOICE_UPDATE = 'invoice:update',
  INVOICE_DELETE = 'invoice:delete',
  INVOICE_VALIDATE = 'invoice:validate',

  QUOTE_CREATE = 'quote:create',
  QUOTE_READ = 'quote:read',
  QUOTE_UPDATE = 'quote:update',
  QUOTE_DELETE = 'quote:delete',

  PAYMENT_CREATE = 'payment:create',
  PAYMENT_READ = 'payment:read',
  PAYMENT_REFUND = 'payment:refund',

  CLIENT_MANAGE = 'client:manage',
  SUPPLIER_MANAGE = 'supplier:manage',
  PRODUCT_MANAGE = 'product:manage',
  SERVICE_MANAGE = 'service:manage',

  ACCOUNTING_READ = 'accounting:read',
  ACCOUNTING_MANAGE = 'accounting:manage',

  REPORT_READ = 'report:read',
  REPORT_EXPORT = 'report:export',

  USER_MANAGE = 'user:manage',
  ROLE_MANAGE = 'role:manage',
  SETTINGS_MANAGE = 'settings:manage',
  AUDIT_READ = 'audit:read',
}

/**
 * Matrice par défaut rôle → permissions, utilisée en repli lorsque le backend
 * ne renvoie pas explicitement la liste des permissions de l'utilisateur.
 */
export const ROLE_PERMISSIONS: Readonly<Record<UserRole, readonly Permission[]>> = {
  [UserRole.ADMIN]: Object.values(Permission),
  [UserRole.COMPTABLE]: [
    Permission.INVOICE_CREATE,
    Permission.INVOICE_READ,
    Permission.INVOICE_UPDATE,
    Permission.INVOICE_VALIDATE,
    Permission.QUOTE_READ,
    Permission.PAYMENT_READ,
    Permission.ACCOUNTING_READ,
    Permission.ACCOUNTING_MANAGE,
    Permission.REPORT_READ,
    Permission.REPORT_EXPORT,
  ],
  [UserRole.GESTIONNAIRE]: [
    Permission.INVOICE_CREATE,
    Permission.INVOICE_READ,
    Permission.INVOICE_UPDATE,
    Permission.QUOTE_CREATE,
    Permission.QUOTE_READ,
    Permission.QUOTE_UPDATE,
    Permission.CLIENT_MANAGE,
    Permission.SUPPLIER_MANAGE,
    Permission.PRODUCT_MANAGE,
    Permission.SERVICE_MANAGE,
    Permission.REPORT_READ,
  ],
  [UserRole.CAISSIER]: [
    Permission.PAYMENT_CREATE,
    Permission.PAYMENT_READ,
    Permission.INVOICE_READ,
  ],
  [UserRole.AUDITEUR]: [
    Permission.INVOICE_READ,
    Permission.QUOTE_READ,
    Permission.PAYMENT_READ,
    Permission.ACCOUNTING_READ,
    Permission.REPORT_READ,
    Permission.AUDIT_READ,
  ],
  [UserRole.UTILISATEUR]: [Permission.INVOICE_READ, Permission.QUOTE_READ],
};
