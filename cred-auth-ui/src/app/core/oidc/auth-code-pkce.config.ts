import { AuthConfig } from 'angular-oauth2-oidc';

/**
 * OAuth2/OIDC settings for local Authorization Code + PKCE.
 *
 * Matches the Authorization Server (auth-service) running at http://localhost:9000
 * and the SPA redirect route at http://localhost:4200/auth/callback
 */
export const authCodePkceConfig: AuthConfig = {
  issuer: 'http://localhost:9000',
  clientId: 'cred-auth-ui',
  responseType: 'code',
  redirectUri: window.location.origin + '/auth/callback',
  scope: 'openid profile email read write',

  // Local dev (HTTP) convenience:
  requireHttps: false,
  strictDiscoveryDocumentValidation: false,
  showDebugInformation: true,

  // PKCE is used for Authorization Code flow (no extra flag needed here).
};

