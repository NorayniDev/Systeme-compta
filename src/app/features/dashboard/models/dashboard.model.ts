export interface IDashboardKpis {
  revenue: number;
  revenueTrendPercent: number;
  invoicesCount: number;
  invoicesTrendPercent: number;
  paymentsReceived: number;
  receivables: number;
  payables: number;
  activeClients: number;
  productsSold: number;
}

export interface IRevenueChartPoint {
  month: string;
  revenue: number;
  expenses: number;
}

export interface IInvoiceStatusBreakdown {
  status: string;
  count: number;
}

export type ActivityType = 'invoice' | 'payment' | 'client' | 'quote' | 'system';

export interface IRecentActivity {
  id: string;
  type: ActivityType;
  message: string;
  createdAt: string;
}
