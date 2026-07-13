/**
 * Enveloppe standard des réponses REST du backend Spring Boot.
 */
export interface IApiResponse<T> {
  data: T;
  message?: string;
  timestamp: string;
}

/**
 * Structure d'erreur normalisée renvoyée par le backend (ou reconstituée par
 * l'intercepteur d'erreurs lorsque le backend est injoignable).
 */
export interface IApiError {
  status: number;
  code: string;
  message: string;
  errors?: Record<string, string[]>;
  timestamp: string;
  path?: string;
}
