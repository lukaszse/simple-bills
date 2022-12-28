import { Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { BehaviorSubject, catchError, debounceTime, Observable, retry, Subject, switchMap, tap } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { CategoryUsageLimitModel } from "../dto/category-usage-limit.model";
import { HttpUtils } from "../utils/http-client.utils";
import { CategoryUsagePieChartModel } from "../dto/category-usage.pie.chart.model";

@Injectable({providedIn: "root"})
export class UsageLimitPieChartService {

  private static host = environment.planningHost
  private static endpoint = "/category-usage-limit"

  private _findCategoryUsageLimit$ = new Subject<void>();
  private _findTotalUsageLimit$ = new Subject<void>();
  private _categoryUsagePieChart$ = new BehaviorSubject<CategoryUsagePieChartModel[]>(null);
  private _loading$ = new BehaviorSubject<boolean>(true);


  constructor(private httpClient: HttpClient) {
    this._findCategoryUsageLimit$
      .pipe(
        tap(() => this._loading$.next(true)),
        debounceTime(200),
        switchMap(() => this.findCategoryUsageLimits()),
        tap(() => this._loading$.next(false))
      )
      .subscribe((result) => {
        this._categoryUsagePieChart$.next(UsageLimitPieChartService.preparePieCharData(result))
      });
  }

  refresh(): void {
    this._findCategoryUsageLimit$.next();
    this._findTotalUsageLimit$.next()
  }

  get categoryUsagePieChart$(): Observable<CategoryUsagePieChartModel[]> {
    return this._categoryUsagePieChart$.asObservable();
  }

  get loading$(): Observable<boolean> {
    return this._loading$.asObservable();
  }

  private findCategoryUsageLimits(total?: boolean): Observable<CategoryUsageLimitModel[]> {
    const url = HttpUtils.prepareUrl(UsageLimitPieChartService.host, UsageLimitPieChartService.endpoint);
    const completeUrl = total ? `${url}?total=true` : url;
    return this.httpClient
      .get<UsageLimitPieChartService[]>(completeUrl, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        retry({count: 3, delay: 1000}),
        tap(console.log),
        catchError(HttpUtils.handleError),
      );
  }

  private static preparePieCharData(categoryUsageLimits: CategoryUsageLimitModel[]): CategoryUsagePieChartModel[] {
    const totalUsage: number = UsageLimitPieChartService.calculateTotalUsage(categoryUsageLimits);
    return categoryUsageLimits
      .map(categoryUsageLimit =>
        new CategoryUsagePieChartModel(UsageLimitPieChartService.prepareCategoryNameLabel(categoryUsageLimit, totalUsage), categoryUsageLimit.usage));
  }

  private static calculateTotalUsage(categoryUsageLimits: CategoryUsageLimitModel[]) {
    return categoryUsageLimits
      .map(categoryUsageLimit => categoryUsageLimit.usage)
      .reduce((accumulator, currentValue) => accumulator + currentValue, 0);
  }

  private static prepareCategoryNameLabel(categoryUsageLimit: CategoryUsageLimitModel, totalUsage: number): string {
    const percentageUsage: string = `${Math.round((categoryUsageLimit.usage / totalUsage) * 100)}%`
    return `${categoryUsageLimit.categoryName} (${percentageUsage})`;
  }
}
