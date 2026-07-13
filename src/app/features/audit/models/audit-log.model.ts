/** Nature de l'action tracée dans le journal d'audit. */
export enum AuditAction {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  LOGIN = 'LOGIN',
  VALIDATE = 'VALIDATE',
  EXPORT = 'EXPORT',
}

/**
 * Entrée du journal d'audit: trace immuable d'une action effectuée par un
 * utilisateur sur une ressource métier. Lecture seule côté frontend — les
 * entrées sont produites par le backend à chaque opération sensible.
 */
export interface IAuditLog {
  id: string;
  timestamp: string;
  userName: string;
  action: AuditAction;
  entityType: string;
  entityLabel: string;
  details?: string;
}
