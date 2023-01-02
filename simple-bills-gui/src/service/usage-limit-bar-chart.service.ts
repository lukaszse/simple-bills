import { HttpClient } from "@angular/common/http";
import { environment } from "../environments/environment";
import { BehaviorSubject, catchError, debounceTime, Observable, retry, Subject, switchMap, tap } from "rxjs";
import { CategoryUsageLimitModel } from "../dto/category-usage-limit.model";
import { HttpUtils } from "../utils/http-client.utils";
import { Injectable } from "@angular/core";
import { CategoryUsageLimitBarChartModel } from "../dto/category-usage-limit-bar-chart.model";
import { CurrencyPipe } from "@angular/common";
import { setToZeroIfNull } from "../utils/simple-bills.utils";


@Injectable({providedIn: "root"})
export class UsageLimitBarChartService {

  private static host = environment.planningHost
  private static endpoint = "/category-usage-limit"

  private _findCategoryUsageLimit$ = new Subject<void>();
  private _findTotalUsageLimit$ = new Subject<void>();
  private _categoryUsageBarChart$ = new BehaviorSubject<CategoryUsageLimitBarChartModel[][]>(null);
  private _totalUsageLimitBarChart$ = new BehaviorSubject<CategoryUsageLimitBarChartModel[][]>(null);
  private _loadingCategories$ = new BehaviorSubject<boolean>(true);
  private _loadingTotal$ = new BehaviorSubject<boolean>(true);


  constructor(private httpClient: HttpClient, private currencyPipe: CurrencyPipe) {

    this._findCategoryUsageLimit$
      .pipe(
        tap(() => this._loadingCategories$.next(true)),
        debounceTime(200),
        switchMap(() => this.findCategoryUsageLimits()),
        tap(() => this._loadingCategories$.next(false))
      )
      .subscribe((result) => {
        this._categoryUsageBarChart$.next(UsageLimitBarChartService.prepareBarCharData(result, currencyPipe))
      });

    this._findTotalUsageLimit$
      .pipe(
        tap(() => this._loadingTotal$.next(true)),
        debounceTime(200),
        switchMap(() => this.findCategoryUsageLimits(true)),
        tap(() => this._loadingTotal$.next(false))
      )
      .subscribe((result) => {
        this._totalUsageLimitBarChart$.next(UsageLimitBarChartService.prepareBarCharData(result, currencyPipe))
      });
  }

  private findCategoryUsageLimits(total?: boolean): Observable<CategoryUsageLimitModel[]> {
    const url = HttpUtils.prepareUrl(UsageLimitBarChartService.host, UsageLimitBarChartService.endpoint);
    const completeUrl = total ? `${url}?total=true` : url;
    return this.httpClient
      .get<UsageLimitBarChartService[]>(completeUrl, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        tap(console.log),
        retry({count: 3, delay: 1000}),
        catchError(HttpUtils.handleError),
      );
  }

  refresh() {
    this._findCategoryUsageLimit$.next();
    this._findTotalUsageLimit$.next()
  }

  get categoryUsageBarChart$() {
    return this._categoryUsageBarChart$.asObservable();
  }

  get totalUsageLimitBarchart$() {
    return this._totalUsageLimitBarChart$.asObservable();
  }

  get loadingCategories$() {
    return this._loadingCategories$.asObservable();
  }

  get loadingTotal$() {
    return this._loadingTotal$.asObservable();
  }

  private static prepareBarCharData(categoryUsageLimits: CategoryUsageLimitModel[],
                                    currencyPipe: CurrencyPipe): CategoryUsageLimitBarChartModel[][] {
    return categoryUsageLimits
      .map(categoryUsageLimit => UsageLimitBarChartService.convertToBarChartData(categoryUsageLimit, currencyPipe));
  }

  private static convertToBarChartData(categoryUsageLimit: CategoryUsageLimitModel,
                                       currencyPipe: CurrencyPipe): CategoryUsageLimitBarChartModel[] {
    const remainingLimit: number = UsageLimitBarChartService.calculateRemainingLimit(categoryUsageLimit);
    const limit = setToZeroIfNull(categoryUsageLimit.limit);
    const formattedUsage: string = currencyPipe.transform(categoryUsageLimit.usage, 'USD', 'symbol', '1.2-2');
    const formattedLimit: string = currencyPipe.transform(categoryUsageLimit.limit, 'USD', 'symbol', '1.2-2');
    const nameWithUsageAndLimit: string = `${categoryUsageLimit.categoryName} (${formattedUsage}/${formattedLimit})`
    return [new CategoryUsageLimitBarChartModel(categoryUsageLimit.categoryName, nameWithUsageAndLimit, categoryUsageLimit.usage, remainingLimit, limit)];
  }

  private static calculateRemainingLimit(categoryUsageLimit: CategoryUsageLimitModel): number {
    if (categoryUsageLimit.limit && categoryUsageLimit.usage <= categoryUsageLimit.limit) {
      return categoryUsageLimit.limit - categoryUsageLimit.usage;
    } else {
      return 0;
    }
  }
}
