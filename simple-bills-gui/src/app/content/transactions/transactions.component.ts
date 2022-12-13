import {Component, OnInit, QueryList, ViewChildren} from '@angular/core';
import {DatePipe, DecimalPipe, formatDate} from "@angular/common";
import {NgbdSortableHeader, SortEvent, SortUtils} from "../../../utils/sortable.directive";
import {TransactionSearchService} from "../../../service/transaction-search.service";
import {TransactionCrudService} from "../../../service/transaction-crud.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {TransactionDto} from "../../../dto/transactionDto";
import {Category} from "../../../dto/category";
import {CategoryService} from "../../../service/category.service";
import {Transaction, TransactionType} from "../../../dto/transaction";
import {BalanceService} from "../../../service/balance.service";
import {Observable} from "rxjs";


@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.css'],
  providers: [DecimalPipe, DatePipe]
})
export class TransactionsComponent implements OnInit {

  transactionDto: TransactionDto = {
    transactionNumber: null,
    type: null,
    category: null,
    description: null,
    amount: null,
    date: null
  };

  category: Category = {
    name: null,
    transactionType: null,
    limit: null
  };

  categoriesToSelect$: Observable<Category[]>;
  selectedTransaction: string | number;
  @ViewChildren(NgbdSortableHeader) headers: QueryList<NgbdSortableHeader>;

  constructor(public transactionSearchService: TransactionSearchService,
              public categoryService: CategoryService,
              public balanceService: BalanceService,
              private transactionCrudService: TransactionCrudService,
              private modalService: NgbModal) {
  }

  ngOnInit(): void {
    this.transactionSearchService.refresh();
    this.balanceService.refresh();
  }

  onSort({column, direction}: SortEvent) {
    this.headers = SortUtils.resetOtherHeaders(this.headers, column);
    this.transactionSearchService.sortColumn = column;
    this.transactionSearchService.sortDirection = direction;
  }

  openTransactionCreationWindow(transactionType: string, content) {
    this.resetFormFields(TransactionType[transactionType])
    this.modalService.open(content, {ariaLabelledBy: 'modal-transaction-creation'}).result
      .then(
        () => this.transactionCrudService.createTransaction(this.transactionDto)
          .subscribe(() => this.ngOnInit()),
        () => console.log(`Transaction (${transactionType}) creation canceled`));
  }

  openTransactionEditWindow(transaction: Transaction, content) {
    this.selectedTransaction = transaction.transactionNumber;
    this.setFormFields(transaction)
    this.modalService.open(content, {ariaLabelledBy: 'modal-transaction-update'}).result.then(
      () => {
        const transactionToUpdate: Transaction = this.prepareTransactionToUpdate(transaction);
        this.transactionCrudService.updateTransaction(transactionToUpdate)
          .subscribe(() => this.ngOnInit());
      },
      () => console.log("Transaction update canceled"));
  }

  openDeletionConfirmationWindow(transactionNumber: number | string, content) {
    this.selectedTransaction = transactionNumber;
    this.modalService.open(content, {ariaLabelledBy: "modal-transaction-deletion"}).result.then(
      () => this.transactionCrudService.deleteBill(transactionNumber)
        .subscribe(() => this.ngOnInit()),
      () => console.log("Transaction deletion canceled"));
  }

  resetFormFields(transactionType?: TransactionType) {
    this.categoriesToSelect$ = this.categoryService.findCategories(transactionType);
    this.transactionDto.type = transactionType;
    this.transactionDto.category = null;
    this.transactionDto.description = null;
    this.transactionDto.amount = null;
    this.transactionDto.date = formatDate(Date.now(), "yyyy-MM-dd", "en");
  }

  setFormFields(transaction: Transaction) {
    this.categoriesToSelect$ = this.categoryService.findCategories(transaction.type);
    this.transactionDto.transactionNumber = transaction.transactionNumber
    this.transactionDto.type = transaction.type;
    this.transactionDto.category = transaction.category;
    this.transactionDto.description = transaction.description;
    this.transactionDto.amount = Math.abs(transaction.amount);
    this.transactionDto.date = formatDate(transaction.date, "yyyy-MM-dd", "en");
  }

  prepareTransactionToUpdate(transaction: Transaction): Transaction {
    transaction.type = this.transactionDto.type;
    transaction.category = this.transactionDto.category;
    transaction.description = this.transactionDto.description;
    transaction.amount = this.transactionDto.amount;
    transaction.date = this.transactionDto.date;
    return transaction;
  }
}
