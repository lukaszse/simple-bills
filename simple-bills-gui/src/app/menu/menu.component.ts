import { Component, OnInit } from '@angular/core';
import { Oauth2Service } from "../../service/oauth2.service";
import { UserService } from "../../service/user.service";

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {


  public isLogged: boolean = false;

  constructor(private oauth2Service: Oauth2Service, private userService: UserService) {
  }

  ngOnInit() {
    this.isLogged = this.oauth2Service.checkCredentials();
    let i = window.location.href.indexOf('code');
    if (!this.isLogged && i != -1) {
      this.oauth2Service.retrieveToken(window.location.href.substring(i + 5));
    }
  }

  login() {
    window.location.href = this.oauth2Service.prepareOAuthProviderLoginUrl();
    this.userService.addUserLogging()
  }

  logout() {
    this.oauth2Service.deleteTokenCookie();
    window.location.href = this.oauth2Service.redirectUri;
  }
}
