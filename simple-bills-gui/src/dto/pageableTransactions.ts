import { Transaction } from "./transaction";

export class PageableTransactions {

  transactions: Transaction[]
  totalCount: number
  pageTotalAmount?: number

  constructor(bills: Transaction[], totalCount: number, totalAmount?: number) {
    this.transactions = bills;
    this.totalCount = totalCount;
    this.pageTotalAmount = totalAmount;
  }
}
