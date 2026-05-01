import { APP_INITIALIZER, ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { OAuthModule, OAuthService } from 'angular-oauth2-oidc';

import { routes } from './app.routes';
import { authCodePkceConfig } from './core/oidc/auth-code-pkce.config';

function initOAuth(oauthService: OAuthService) {
  return async () => {
    oauthService.configure(authCodePkceConfig);
    // Don't block the whole SPA from rendering if the auth-service is down
    // or discovery fails during local dev.
    try {
      // Loads OIDC discovery document and tries to parse tokens from callback URL if present.
      await oauthService.loadDiscoveryDocumentAndTryLogin();
    } catch (e) {
      // Keep app usable (home/login routes still render); login can be tried later.
      console.error('OIDC init failed (discovery/tryLogin). App will continue.', e);
    }
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideHttpClient(),
    importProvidersFrom(OAuthModule.forRoot()),
    provideRouter(routes),
    {
      provide: APP_INITIALIZER,
      useFactory: initOAuth,
      deps: [OAuthService],
      multi: true
    }
  ]
};
