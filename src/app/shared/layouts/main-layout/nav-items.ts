import { UserRole } from '../../../core/constants/roles.constant';

export interface INavItem {
  labelKey: string;
  icon: string;
  route: string;
  roles?: UserRole[];
}

export interface INavSection {
  titleKey: string;
  items: INavItem[];
}

/**
 * Structure de navigation du layout principal, regroupée par domaine
 * fonctionnel. `roles` restreint la visibilité de l'entrée (RBAC).
 * `labelKey`/`titleKey` sont des clés ngx-translate résolues dans le template
 * via le pipe `translate` (voir `public/i18n/fr.json` et `en.json`).
 */
export const NAV_SECTIONS: INavSection[] = [
  {
    titleKey: 'nav.section.general',
    items: [{ labelKey: 'nav.dashboard', icon: 'dashboard', route: '/dashboard' }],
  },
  {
    titleKey: 'nav.section.sales',
    items: [
      { labelKey: 'nav.clients', icon: 'groups', route: '/clients' },
      { labelKey: 'nav.quotes', icon: 'request_quote', route: '/quotes' },
      { labelKey: 'nav.invoices', icon: 'receipt_long', route: '/invoices' },
      { labelKey: 'nav.payments', icon: 'payments', route: '/payments' },
    ],
  },
  {
    titleKey: 'nav.section.purchasingCatalog',
    items: [
      { labelKey: 'nav.suppliers', icon: 'local_shipping', route: '/suppliers' },
      { labelKey: 'nav.products', icon: 'inventory_2', route: '/products' },
      { labelKey: 'nav.services', icon: 'design_services', route: '/services' },
    ],
  },
  {
    titleKey: 'nav.section.accounting',
    items: [
      { labelKey: 'nav.journal', icon: 'menu_book', route: '/accounting/journal' },
      { labelKey: 'nav.ledger', icon: 'auto_stories', route: '/accounting/ledger' },
      { labelKey: 'nav.trialBalance', icon: 'balance', route: '/accounting/trial-balance' },
      { labelKey: 'nav.cashbook', icon: 'point_of_sale', route: '/accounting/cashbook' },
    ],
  },
  {
    titleKey: 'nav.section.oversight',
    items: [
      { labelKey: 'nav.reports', icon: 'bar_chart', route: '/reports' },
      {
        labelKey: 'nav.audit',
        icon: 'fact_check',
        route: '/audit',
        roles: [UserRole.ADMIN, UserRole.AUDITEUR],
      },
    ],
  },
  {
    titleKey: 'nav.section.administration',
    items: [
      { labelKey: 'nav.users', icon: 'manage_accounts', route: '/users', roles: [UserRole.ADMIN] },
      {
        labelKey: 'nav.roles',
        icon: 'admin_panel_settings',
        route: '/roles',
        roles: [UserRole.ADMIN],
      },
      {
        labelKey: 'nav.settings',
        icon: 'settings',
        route: '/settings',
        roles: [UserRole.ADMIN],
      },
    ],
  },
];
