import { HttpClient } from "@angular/common/http";
import { environment } from "../environments/environment";
import { Balance } from "../dto/balance";
import { BehaviorSubject, catchError, debounceTime, Observable, retry, Subject, switchMap, tap } from "rxjs";
import { HttpUtils } from "../utils/httpClientUtils";
import { Injectable } from "@angular/core";

@Injectable({providedIn: "root"})
export class BalanceService {

  private static host = environment.planningHost;
  private static endpoint = "/balance";

  private _findBalance$ = new Subject<void>()
  private _balance$ = new BehaviorSubject<Balance>(null);
  private _loading$ = new BehaviorSubject<boolean>(true);


  constructor(private httpClient: HttpClient) {
    this._findBalance$
      .pipe(
        tap(() => this._loading$.next(true)),
        debounceTime(200),
        switchMap(() => this.findBalance()),
        tap(() => this._loading$.next(false))
      )
      .subscribe((result) => this._balance$.next(result))
  }

  findBalance(): Observable<Balance> {
    const url = HttpUtils.prepareUrl(BalanceService.host, BalanceService.endpoint);
    return this.httpClient.get<Balance>(url, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        catchError(HttpUtils.handleError),
        retry({count: 3, delay: 1000})
      );
  }

  refresh() {
    this._findBalance$.next();
  }

  get balance$() {
    return this._balance$.asObservable();
  }

  get loading$() {
    return this._loading$.asObservable();
  }
}
