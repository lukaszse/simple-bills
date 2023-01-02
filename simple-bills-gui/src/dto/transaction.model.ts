export interface TransactionModel {
  user?: string;
  transactionNumber?: string | number;
  type?: TransactionType;
  category?: string;
  description?: string;
  amount?: number;
  date?: string;
}

export enum TransactionType {
  INCOME = "INCOME",
  EXPENSE = "EXPENSE"
}
