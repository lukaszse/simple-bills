// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {

  production: false,
  transactionManagementHost: "http://host.docker.internal:8081",
  planningHost: "http://host.docker.internal:8082",
  assetManagementHost: "http://host.docker.internal:8083",
  redirectUri: "http://host.docker.internal:8080/",
  tokenUrl: "http://host.docker.internal:8085/realms/simple-bills/protocol/openid-connect/token",
  authUrl: "http://host.docker.internal:8085/realms/simple-bills/protocol/openid-connect/auth",
  clientId: "simple-bills-local-view"
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
