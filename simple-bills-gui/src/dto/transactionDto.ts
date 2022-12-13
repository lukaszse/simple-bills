import { TransactionType } from "./transaction";

export interface TransactionDto {
  transactionNumber?: string | number;
  type?: TransactionType,
  category?: string;
  description?: string;
  amount?: number;
  date?: string;
}
