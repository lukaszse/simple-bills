import { TransactionType } from "./transaction";

export interface Category {
  name?: string;
  transactionType?: TransactionType;
  limit?: number;
}
