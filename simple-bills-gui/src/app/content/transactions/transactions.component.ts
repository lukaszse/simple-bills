import { Component, OnInit, QueryList, ViewChildren } from '@angular/core';
import { DatePipe, DecimalPipe, formatDate } from "@angular/common";
import { NgbdSortableHeader, SortEvent, SortUtils } from "../../../utils/sortable.directive";
import { TransactionSearchService } from "../../../service/transaction-search.service";
import { TransactionCrudService } from "../../../service/transaction-crud.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { CategoryModel } from "../../../dto/category.model";
import { CategoryService } from "../../../service/category.service";
import { TransactionModel, TransactionType } from "../../../dto/transaction.model";
import { BalanceService } from "../../../service/balance.service";
import { delay, first, Observable } from "rxjs";
import { DATE_FORMAT, LOCALE_EN } from "../../../utils/simple-bills.constants"


@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss'],
  providers: [DecimalPipe, DatePipe]
})
export class TransactionsComponent implements OnInit {

  transactionDto: TransactionModel = {
    transactionNumber: null,
    type: null,
    category: null,
    description: null,
    amount: null,
    date: null
  };

  category: CategoryModel = {
    name: null,
    transactionType: null,
    limit: null
  };

  categoriesToSelect$: Observable<CategoryModel[]>;
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
          .pipe(first(), delay(1000))
          .subscribe(() =>
            this.ngOnInit()),
        () => console.log(`Transaction (${transactionType}) creation canceled`));
  }

  openTransactionEditWindow(transaction: TransactionModel, content) {
    this.selectedTransaction = transaction.transactionNumber;
    this.setFormFields(transaction)
    this.modalService.open(content, {ariaLabelledBy: 'modal-transaction-update'}).result.then(
      () => {
        const transactionToUpdate: TransactionModel = this.prepareTransactionToUpdate(transaction);
        this.transactionCrudService.updateTransaction(transactionToUpdate)
          .pipe(first(), delay(1000))
          .subscribe(() => this.ngOnInit());
      },
      () => console.log("TransactionModel update canceled"));
  }

  openDeletionConfirmationWindow(transactionNumber: number | string, content) {
    this.selectedTransaction = transactionNumber;
    this.modalService.open(content, {ariaLabelledBy: "modal-transaction-deletion"}).result.then(
      () => this.transactionCrudService.deleteTransaction(transactionNumber)
        .pipe(first(), delay(1000))
        .subscribe(() => this.ngOnInit()),
      () => console.log("TransactionModel deletion canceled"));
  }

  private resetFormFields(transactionType?: TransactionType) {
    this.categoriesToSelect$ = this.categoryService.findCategories(transactionType);
    this.transactionDto.type = transactionType;
    this.transactionDto.category = null;
    this.transactionDto.description = null;
    this.transactionDto.amount = null;
    this.transactionDto.date = formatDate(Date.now(), DATE_FORMAT, LOCALE_EN);
  }

  private setFormFields(transaction: TransactionModel) {
    this.categoriesToSelect$ = this.categoryService.findCategories(transaction.type);
    this.transactionDto.transactionNumber = transaction.transactionNumber
    this.transactionDto.type = transaction.type;
    this.transactionDto.category = transaction.category;
    this.transactionDto.description = transaction.description;
    this.transactionDto.amount = Math.abs(transaction.amount);
    this.transactionDto.date = formatDate(transaction.date, DATE_FORMAT, LOCALE_EN);
  }

  private prepareTransactionToUpdate(transaction: TransactionModel): TransactionModel {
    transaction.type = this.transactionDto.type;
    transaction.category = this.transactionDto.category;
    transaction.description = this.transactionDto.description;
    transaction.amount = this.transactionDto.amount;
    transaction.date = this.transactionDto.date;
    return transaction;
  }
}
