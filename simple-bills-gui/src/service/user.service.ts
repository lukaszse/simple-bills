import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, retry, tap } from "rxjs";
import { HttpUtils } from "../utils/http-client.utils";
import { environment } from "../environments/environment";
import { User } from "../dto/user";
import { map } from "rxjs/operators";


@Injectable({providedIn: "root"})
export class UserService {

  private static host: string = environment.transactionManagementHost;
  private static userInfoEndpoint: string = "/user/info";
  private static userLoggingActivityEndpoint: string = "/user/logging"

  constructor(private httpClient: HttpClient) {
  }

  getUser(): Observable<string> {
    return this.httpClient
      .get<User>(HttpUtils.prepareUrl(UserService.host, UserService.userInfoEndpoint),
        {headers: HttpUtils.prepareHeaders(), observe: 'response'})
      .pipe(
        tap(console.log),
        retry({count: 3, delay: 1000}),
        map(response => response.body),
        map(this.getShowUserName)
      );
  }

  addUserLogging(): void {
    this.httpClient
      .post<User>(HttpUtils.prepareUrl(UserService.host, UserService.userLoggingActivityEndpoint),
        {headers: HttpUtils.prepareHeaders(), observe: 'response'})
      .pipe(
        tap(console.log),
        retry({count: 3, delay: 1000})
      );
  }

  getShowUserName(user: User): string {
    if (user.givenName) {
      return user.givenName;
    } else if (user.name) {
      return user.name;
    } else if (user.preferredUsername) {
      return user.preferredUsername;
    }
  }
}
