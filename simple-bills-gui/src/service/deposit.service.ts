import { Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { BehaviorSubject, catchError, debounceTime, Observable, retry, Subject, switchMap, tap } from "rxjs";
import { DepositModel } from "../dto/deposit.model";
import { HttpClient } from "@angular/common/http";
import { BalanceModel } from "../dto/balance.model";
import { HttpUtils } from "../utils/http-client.utils";
import { TransactionModel } from "../dto/transaction.model";

@Injectable({providedIn: "root"})
export class DepositService {

  private static host = environment.assetManagementHost;
  private static endpoint = "/deposits";

  private _findDeposits$ = new Subject<void>()
  private _deposits$ = new BehaviorSubject<DepositModel[]>(null);
  private _loading$ = new BehaviorSubject<boolean>(true);

  constructor(private httpClient: HttpClient) {
    this._findDeposits$
      .pipe(
        tap(() => this._loading$.next(true)),
        debounceTime(200),
        switchMap(() => this.findDeposits()),
        tap(() => this._loading$.next(false))
      )
      .subscribe((result) => this._deposits$.next(result))
  }

  private findDeposits(): Observable<DepositModel[]> {
    const url = HttpUtils.prepareUrl(DepositService.host, DepositService.endpoint);
    return this.httpClient.get<BalanceModel>(url, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        tap(console.log),
        retry({count: 3, delay: 1000}),
        catchError(HttpUtils.handleError),
      );
  }

  createDeposit(deposit: DepositModel): Observable<string | Object> {
    const url = HttpUtils.prepareUrl(DepositService.host, DepositService.endpoint);
    return this.httpClient
      .post<string>(url, deposit, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        tap(strResponse => console.log(`Transaction with transactionNumber=${strResponse} created.`)),
        retry({count: 3, delay: 1000}),
        tap(() => this.refresh()),
        catchError(HttpUtils.handleError)
      )
  }

  updateDeposit(deposit: DepositModel): Observable<TransactionModel> {
    const url = `${HttpUtils.prepareUrlWithId(DepositService.host, DepositService.endpoint, deposit.name)}`;
    return this.httpClient
      .patch<TransactionModel>(url, deposit, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        tap((updatedBill) => console.log(`Transaction with transactionNumber=${updatedBill.transactionNumber} updated.`)),
        retry({count: 3, delay: 1000}),
        tap(() => this.refresh()),
        catchError(HttpUtils.handleError)
      )
  }

  deleteDeposit(transactionNumber: number | string): Observable<number | Object> {
    const url = `${HttpUtils.prepareUrlWithId(DepositService.host, DepositService.endpoint, transactionNumber)}`;
    return this.httpClient
      .delete<string>(url, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        tap(() => console.log(`Transaction with transactionNumber=${transactionNumber} deleted.`)),
        retry({count: 3, delay: 1000}),
        tap(() => this.refresh()),
        catchError(HttpUtils.handleError)
      )
  }

  refresh() {
    this._findDeposits$.next();
  }

  get deposits$() {
    return this._deposits$.asObservable();
  }

  get loading$() {
    return this._loading$.asObservable();
  }
}
