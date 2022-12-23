import { HttpErrorResponse, HttpHeaders } from "@angular/common/http";
import { Cookie } from "ng2-cookies";
import { EMPTY } from "rxjs";


export class HttpUtils {

  public static X_TOTAL_COUNT = 'x-total-count';
  public static PAGE_SIZE = "pageSize";
  public static PAGE_NUMBER = "pageNumber";
  public static DATE_FROM = "dateFrom";
  public static DATE_TO = "dateTo";
  public static SORT_DIRECTION = "sortDirection";
  public static SORT_COLUMN = "sortColumn";


  public static prepareUrl(host: string,
                           endpoint: string): string;
  public static prepareUrl(host: string,
                           endpoint: string,
                           pageSize: number): string;
  public static prepareUrl(host: string,
                           endpoint: string,
                           pageSize: number,
                           pageNumber: number,
                           sortDirection: string,
                           sortColumn: string,
                           dateFrom: Date,
                           dateTo: Date): string;
  public static prepareUrl(host?: string,
                           endpoint?: string,
                           pageSize?: number,
                           pageNumber?: number,
                           sortDirection?: string,
                           sortColumn?: string,
                           dateFrom?: Date,
                           dateTo?: Date): string {
    let url = `${host}${endpoint}`;
    const queryParams: string [] = [];
    if (pageSize) {
      queryParams.push(HttpUtils.getUrlParam(this.PAGE_SIZE, pageSize.toString()))
    }
    if (pageNumber) {
      queryParams.push(HttpUtils.getUrlParam(this.PAGE_NUMBER, pageNumber.toString()))
    }
    if (sortDirection) {
      queryParams.push(HttpUtils.getUrlParam(this.SORT_DIRECTION, sortDirection.toUpperCase()))
    }
    if (sortColumn) {
      queryParams.push(HttpUtils.getUrlParam(this.SORT_COLUMN, sortColumn))
    }
    if (dateFrom) {
      queryParams.push(HttpUtils.getUrlParam(this.DATE_FROM, dateFrom.toString()))
    }
    if (dateTo) {
      queryParams.push(HttpUtils.getUrlParam(this.DATE_TO, dateTo.toString()))
    }
    return queryParams.length === 0 ?
      url : `${url}?${queryParams.join("&")}`;
  }

  static prepareUrlWithId(host: string, endpoint: string, id: string | number) {
    return `${host}${endpoint}/${id}`;
  }

  public static prepareHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + Cookie.get('access_token')
    });
  }

  public static handleError(error: HttpErrorResponse) {
    if (error.status === 0) {
      console.error('An error occurred:', error.error);
      alert("Some network error detected. Please try again later!")
    } else {
      console.error(
        `Backend returned code ${error.status}, body was: `, error.error);
      switch (error.status) {
        case 409: {
          alert("Error. An object with given name/key already exists!")
        }
      }
    }
    return EMPTY
  }

  public static prepareOAuth2ProviderLoginUri(authUrl: string,
                                              clientId: string,
                                              redirectUri: string,
                                              scopes: string[]) {
    const scopeParam = HttpUtils.prepareScopesParam(scopes);
    return `${authUrl}?response_type=code&scope=${scopeParam}&client_id=${clientId}&redirect_uri=${redirectUri}`;
  }

  public static prepareOAuth2CodeFlowUrlParams(code: string, clientId: string, redirectUri: string): string {
    let params = new URLSearchParams();
    params.append('grant_type', 'authorization_code');
    params.append('client_id', clientId);
    params.append('redirect_uri', redirectUri);
    params.append('code', code);
    return params.toString();
  }

  private static getUrlParam(paramName: string, paramValue: string): string {
    return `${paramName}=${paramValue}`;
  }

  private static prepareScopesParam(scopes: string[]): string {
    return scopes.join("%20");
  }
}


