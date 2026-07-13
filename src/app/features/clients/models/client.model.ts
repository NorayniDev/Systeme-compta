export enum ClientStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export interface IClient {
  id: string;
  name: string;
  email: string;
  phone: string;
  address: string;
  taxId: string;
  status: ClientStatus;
  totalInvoiced: number;
  createdAt: string;
  updatedAt: string;
}
