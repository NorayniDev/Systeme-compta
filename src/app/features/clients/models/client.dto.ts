import { ClientStatus } from './client.model';

/** DTO envoyé lors de la création d'un client. */
export interface ClientCreateDto {
  name: string;
  email: string;
  phone: string;
  address: string;
  taxId: string;
}

/** DTO envoyé lors de la mise à jour d'un client. */
export interface ClientUpdateDto extends ClientCreateDto {
  status: ClientStatus;
}
