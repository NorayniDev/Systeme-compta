export type NotificationType = 'QUOTE_ACCEPTED' | 'PAYMENT_RECEIVED' | 'INVOICE_OVERDUE';

export interface INotification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  relatedEntityType: string | null;
  relatedEntityId: string | null;
  read: boolean;
  createdAt: string;
}

/** Le backend envoie des libellés d'affichage ("Devis", "Facture") plutôt qu'un chemin de route. */
const NOTIFICATION_ENTITY_ROUTES: Record<string, string> = {
  Devis: '/quotes',
  Facture: '/invoices',
};

export function notificationRouterLink(notification: INotification): string[] | null {
  const basePath = notification.relatedEntityType
    ? NOTIFICATION_ENTITY_ROUTES[notification.relatedEntityType]
    : undefined;
  return basePath && notification.relatedEntityId ? [basePath, notification.relatedEntityId] : null;
}
