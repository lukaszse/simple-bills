import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { first, Observable, retry, tap } from "rxjs";
import { HttpUtils } from "../utils/http-client.utils";
import { environment } from "../environments/environment";
import { UserDto } from "../dto/user.dto";
import { map } from "rxjs/operators";


@Injectable({providedIn: "root"})
export class UserService {

  private static host: string = environment.transactionManagementHost;
  private static userInfoEndpoint: string = "/user/info";
  private static userActivityEndpoint: string = "/user/activity"

  constructor(private httpClient: HttpClient) {
  }

  getUser(): Observable<string> {
    return this.httpClient
      .get<UserDto>(HttpUtils.prepareUrl(UserService.host, UserService.userInfoEndpoint),
        {headers: HttpUtils.prepareHeaders(), observe: 'response'})
      .pipe(
        tap(console.log),
        retry({count: 3, delay: 1000}),
        map(response => response.body),
        map(this.getShowUserName)
      );
  }

  addUserLoggingIn(): void {
    this.addUserActivity("LOGGING_IN")

  }

  addUserLoggingOut(): void {
    this.addUserActivity("LOGGING_OUT")
  }

  private addUserActivity(activity: "LOGGING_IN" | "LOGGING_OUT"): void {
    this.httpClient
      .post<UserDto>(HttpUtils.prepareUrl(UserService.host, UserService.userActivityEndpoint),
        {activity: activity},
        {headers: HttpUtils.prepareHeaders(), observe: 'response'})
      .pipe(
        first(),
        tap(console.log),
        retry({count: 3, delay: 1000}),
        map(() => true)
      )
      .subscribe();
  }

  private getShowUserName(user: UserDto): string {
    if (user.givenName) {
      return user.givenName;
    } else if (user.name) {
      return user.name;
    } else if (user.preferredUsername) {
      return user.preferredUsername;
    }
  }
}
