import { TransactionType } from "./transaction.model";

export interface CategoryModel {
  name?: string;
  transactionType?: TransactionType;
  limit?: number;
}
