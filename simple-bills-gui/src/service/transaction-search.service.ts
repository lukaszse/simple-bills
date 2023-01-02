import { Injectable, PipeTransform } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, catchError, debounceTime, Observable, retry, Subject, switchMap, tap } from "rxjs";
import { TransactionModel } from "../dto/transaction.model";
import { map } from "rxjs/operators";
import { DatePipe, DecimalPipe } from "@angular/common";
import { PageableTransactionsModel } from "../dto/pageable-transactions.model";
import { SortableState, SortDirection } from "../utils/sortable.directive";
import { HttpUtils } from "../utils/http-client.utils";
import { environment } from "../environments/environment";
import { isPeriodInvalid } from "../utils/simple-bills.utils";


@Injectable({providedIn: "root"})
export class TransactionSearchService {

  private static host: string = environment.transactionManagementHost;
  private static endpoint: string = "/transactions";
  private _pageableTransactions$ = new BehaviorSubject<PageableTransactionsModel>(null);
  private _loading$ = new BehaviorSubject<boolean>(false);
  private _search$ = new Subject<void>()

  private _state: SortableState = {
    pageSize: 5,
    pageNumber: 1,
    sortDirection: '',
    sortColumn: '',
    dateFrom: null,
    dateTo: null,
    searchTerm: ''
  };

  constructor(private httpClient: HttpClient, private decimalPipe: DecimalPipe, private datePipe: DatePipe) {
    this._search$
      .pipe(
        tap(() => this._loading$.next(true)),
        debounceTime(200),
        switchMap(() => this._search()),
        tap(() => this._loading$.next(false))
      )
      .subscribe((result) => this._pageableTransactions$.next(result));
  }

  public refresh(): void {
    this._search$.next();
  }

  private _search(): Observable<PageableTransactionsModel> {
    const {pageSize, pageNumber, sortDirection, sortColumn, dateFrom, dateTo, searchTerm} = this._state;
    let pageableBills$ = this.findTransactions(pageSize, pageNumber, sortDirection, sortColumn, dateFrom, dateTo).pipe()
    pageableBills$ = TransactionSearchService.search(pageableBills$, searchTerm, this.decimalPipe, this.datePipe)
    return pageableBills$
      .pipe(
        map(pageableBills => TransactionSearchService.setAmountSum(pageableBills)),
        tap(console.log));
  }

  private findTransactions(pageSize: number,
                           pageNumber: number,
                           sortDirection: string,
                           sortColumn: string,
                           dateFrom: Date,
                           dateTo: Date): Observable<PageableTransactionsModel> {

    let url = HttpUtils.prepareUrl(TransactionSearchService.host, TransactionSearchService.endpoint, pageSize, pageNumber, sortDirection, sortColumn, dateFrom, dateTo);
    return this.httpClient.get<TransactionModel[]>(url, {headers: HttpUtils.prepareHeaders(), observe: 'response'})
      .pipe(
        tap(console.log),
        retry({count: 3, delay: 1000}),
        map((response) => {
          return new PageableTransactionsModel(response.body, Number(response.headers.get(HttpUtils.X_TOTAL_COUNT)));
        }),
        catchError(HttpUtils.handleError),
      );
  }

  private static search(transactions: Observable<PageableTransactionsModel>,
                        text: string,
                        decimalPipe: PipeTransform,
                        datePipe: DatePipe): Observable<PageableTransactionsModel> {
    return transactions.pipe(
      map(pageableTransactions => {
        const transactions = this.matchBills(pageableTransactions, text, decimalPipe, datePipe);
        return new PageableTransactionsModel(transactions, pageableTransactions.totalCount);
      }))
  }

  private static setAmountSum(pageableBills: PageableTransactionsModel): PageableTransactionsModel {
    pageableBills.pageTotalAmount = this.countAmountSum(pageableBills.transactions)
    return pageableBills;
  }

  private static countAmountSum(transactions: TransactionModel[]): number {
    return transactions
      .map((transaction) => transaction.amount)
      .map(amount => amount == null ? 0 : amount)
      .reduce((accumulator, currentValue) => accumulator + currentValue, 0);
  }

  private static matchBills(pageableTransaction: PageableTransactionsModel, text: string, decimalPipe: PipeTransform, datePipe: DatePipe) {
    return pageableTransaction.transactions.filter(transaction => {
      const term = text.toLowerCase();
      return decimalPipe.transform(transaction.transactionNumber).includes(term)
        || transaction.type.toString().toLowerCase().includes(term)
        || datePipe.transform(transaction.date).includes(term)
        || decimalPipe.transform(transaction.amount).includes(term)
        || transaction.description.toLowerCase().includes(term)
        || transaction.category.toLowerCase().includes(term);
    });
  }

  // getters and setters to wrapped objects
  get pageableTransactions$() {
    return this._pageableTransactions$.asObservable();
  }

  get loading$() {
    return this._loading$.asObservable();
  }

  get page(): number {
    return this._state.pageNumber;
  }

  get pageSize(): number {
    return this._state.pageSize;
  }

  get searchTerm(): string {
    return this._state.searchTerm;
  }

  get dateFrom(): Date {
    return this._state.dateFrom;
  }

  get dateTo(): Date {
    return this._state.dateTo;
  }

  get errorMsg(): string {
    return isPeriodInvalid(this._state.dateFrom, this._state.dateTo) ?
      "Provided dates are incorrect!" : null;
  }

  set page(page: number) {
    this._set({pageNumber: page});
  }

  set pageSize(pageSize: number) {
    this._set({pageSize});
  }

  set searchTerm(searchTerm: string) {
    this._set({searchTerm});
  }

  set sortColumn(sortColumn: string) {
    this._set({sortColumn});
  }

  set sortDirection(sortDirection: SortDirection) {
    this._set({sortDirection});
  }

  set dateFrom(dateFrom: Date) {
    this._set({dateFrom});
  }

  set dateTo(dateTo: Date) {
    this._set({dateTo});
  }

  private _set(patch: Partial<SortableState>) {
    Object.assign(this._state, patch);
    this.refresh();
  }
}
