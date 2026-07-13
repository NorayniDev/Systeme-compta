/**
 * Clés utilisées pour la persistance locale (localStorage/sessionStorage).
 * Regroupées ici pour éviter les collisions et fautes de frappe.
 */
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'facturation_pme.access_token',
  REFRESH_TOKEN: 'facturation_pme.refresh_token',
  REMEMBER_ME: 'facturation_pme.remember_me',
  CURRENT_USER: 'facturation_pme.current_user',
  THEME: 'facturation_pme.theme',
  LANG: 'facturation_pme.lang',
} as const;
