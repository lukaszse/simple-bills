import { TransactionModel } from "./transaction.model";

export class PageableTransactionsModel {

  transactions: TransactionModel[]
  totalCount: number
  pageTotalAmount?: number

  constructor(bills: TransactionModel[], totalCount: number, totalAmount?: number) {
    this.transactions = bills;
    this.totalCount = totalCount;
    this.pageTotalAmount = totalAmount;
  }
}
