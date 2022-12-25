import { TransactionModel } from "./transaction.model";

export class PageableTransactionsModel {

  transactions: TransactionModel[]
  totalCount: number
  pageTotalAmount?: number

  constructor(transactions: TransactionModel[], totalCount: number, totalAmount?: number) {
    this.transactions = transactions;
    this.totalCount = totalCount;
    this.pageTotalAmount = totalAmount;
  }
}
