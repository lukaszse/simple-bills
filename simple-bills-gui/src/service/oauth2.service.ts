import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Cookie } from "ng2-cookies";
import { environment } from "../environments/environment";
import { catchError, tap } from "rxjs";
import { HttpUtils } from "../utils/http-client.utils";


@Injectable({providedIn: "root"})
export class Oauth2Service {

  private readonly scopes = ["openid", "write", "read"];
  public clientId = environment.clientId;
  public redirectUri = environment.redirectUri;

  constructor(private httpClient: HttpClient) {
  }

  retrieveToken(code) {
    const params = HttpUtils.prepareOAuth2CodeFlowUrlParams(code, this.clientId, this.redirectUri);
    const oauth2TokenUrl = `${environment.tokenUrl}`
    const headers = new HttpHeaders({'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'});
    this.httpClient
      .post(oauth2TokenUrl, params, {headers: headers})
      .pipe(
        tap(data => this.saveToken(data)),
        catchError(HttpUtils.handleError)
      )
      .subscribe();
  }

  saveToken(token) {
    const expireDate = new Date().getTime() + (1000 * token.expires_in);
    Cookie.set("access_token", token.access_token, expireDate);
    console.log('Obtained Access token');
    window.location.href = this.redirectUri;
  }

  checkCredentials() {
    return Cookie.check('access_token');
  }

  deleteTokenCookie() {
    Cookie.delete('access_token');
  }

  prepareOAuthProviderLoginUrl(): string {
    return HttpUtils.prepareOAuth2ProviderLoginUri(environment.authUrl, environment.clientId, environment.redirectUri, this.scopes);
  }
}
