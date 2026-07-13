/**
 * Constantes globales de l'application.
 */
export const APP_CONSTANTS = {
  APP_NAME: 'Système Intelligent de Facturation et de Comptabilité',
  APP_SHORT_NAME: 'FacturationPME',
  DEFAULT_PAGE_SIZE: 10,
  PAGE_SIZE_OPTIONS: [10, 25, 50, 100],
  DEFAULT_LANG: 'fr',
  SUPPORTED_LANGS: ['fr', 'en'] as const,
  DEBOUNCE_SEARCH_MS: 350,
  SESSION_WARNING_BEFORE_EXPIRY_MS: 60_000,
  DEFAULT_CURRENCY: 'XOF',
} as const;

export type SupportedLang = (typeof APP_CONSTANTS.SUPPORTED_LANGS)[number];
