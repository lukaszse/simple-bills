import { Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { HttpClient } from "@angular/common/http";
import { catchError, Observable, retry, tap } from "rxjs";
import { HttpUtils } from "../utils/http-client.utils";
import { CategoryModel } from "../dto/category.model";
import { map } from "rxjs/operators";
import { TransactionType } from "../dto/transaction.model";

@Injectable({providedIn: "root"})
export class CategoryService {

  private static host = environment.planningHost
  private static endpoint = "/categories"

  public categories$: Observable<CategoryModel[]>;


  constructor(private httpClient: HttpClient) {
    this.categories$ = this.findCategories();
  }

  createCategory(category: CategoryModel): Observable<string | Object> {
    const url = HttpUtils.prepareUrl(CategoryService.host, CategoryService.endpoint);
    return this.httpClient
      .post<CategoryModel>(url, category, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        tap(category => console.log(`Category with name ${category.name} created.`)),
        retry({count: 3, delay: 1000}),
        catchError(HttpUtils.handleError)
      )
  }

  updateCategory(category: CategoryModel): Observable<string | Object> {
    const url = HttpUtils.prepareUrlWithId(CategoryService.host, CategoryService.endpoint, category.name);
    return this.httpClient
      .patch<CategoryModel>(url, category, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        tap(category => console.log(`Category with name ${category.name} updated.`)),
        retry({count: 3, delay: 1000}),
        catchError(HttpUtils.handleError)
      )
  }

  findCategories(transactionType?: TransactionType): Observable<CategoryModel[]> {
    const url = HttpUtils.prepareUrl(CategoryService.host, CategoryService.endpoint);
    return this.httpClient.get<CategoryModel[]>(url, {headers: HttpUtils.prepareHeaders()})
      .pipe(
        tap(console.log),
        retry({count: 3, delay: 1000}),
        map(categories => this.filterCategories(categories, transactionType)),
        catchError(HttpUtils.handleError),
      );
  }

  deleteCategory(categoryName: string, categoryToReplace: string): Observable<string | Object> {
    const queryParam = categoryToReplace != null ? `?replacementCategory=${categoryToReplace}` : "";
    const url = HttpUtils.prepareUrlWithId(CategoryService.host, CategoryService.endpoint, categoryName) + queryParam;
    return this.httpClient
      .delete(url, {headers: HttpUtils.prepareHeaders(), observe: 'response'})
      .pipe(
        tap(categoryName => console.log(`Category with name ${categoryName.body} deleted.`)),
        retry({count: 3, delay: 1000}),
        catchError(HttpUtils.handleError)
      )
  }

  private filterCategories(categories: CategoryModel[], transactionType?: TransactionType): CategoryModel[] {
    return transactionType ?
      categories.filter(category => category.transactionType === transactionType) : categories;
  }
}
