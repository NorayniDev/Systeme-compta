/**
 * Points d'entrée REST exposés par le backend Spring Boot.
 * Centralisés ici pour éviter la duplication de chaînes littérales dans les services.
 */
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: 'auth/login',
    LOGOUT: 'auth/logout',
    REFRESH: 'auth/refresh',
    ME: 'auth/me',
    FORGOT_PASSWORD: 'auth/forgot-password',
    RESET_PASSWORD: 'auth/reset-password',
  },
  USERS: 'users',
  ROLES: 'roles',
  CLIENTS: 'clients',
  SUPPLIERS: 'suppliers',
  PRODUCTS: 'products',
  SERVICES: 'services',
  QUOTES: 'quotes',
  INVOICES: 'invoices',
  PAYMENTS: 'payments',
  ACCOUNTING: {
    JOURNAL: 'accounting/journal',
    LEDGER: 'accounting/ledger',
    TRIAL_BALANCE: 'accounting/trial-balance',
    CASHBOOK: 'accounting/cashbook',
  },
  DASHBOARD: {
    KPIS: 'dashboard/kpis',
    REVENUE_CHART: 'dashboard/revenue-chart',
    RECENT_ACTIVITY: 'dashboard/recent-activity',
  },
  REPORTS: 'reports',
  NOTIFICATIONS: 'notifications',
  AUDIT: 'audit-logs',
  PROFILE: {
    UPDATE: 'profile',
    CHANGE_PASSWORD: 'profile/change-password',
  },
  SETTINGS: {
    COMPANY: 'settings/company',
  },
} as const;
