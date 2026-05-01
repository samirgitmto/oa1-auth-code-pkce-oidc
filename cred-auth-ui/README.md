# CredAuthUi

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 20.0.5.

## What this client does (OAuth2/OIDC)

This SPA is an **OAuth 2.0 / OpenID Connect (OIDC) client** that logs in against the local Authorization Server:

- Authorization Server (issuer): `http://localhost:9000` (from `auth-service`)
- SPA base URL: `http://localhost:4200`
- Redirect URI (callback route): `http://localhost:4200/auth/callback`

The flow used is **Authorization Code + PKCE + OIDC** (recommended for SPAs).

## Key libraries used

- `angular-oauth2-oidc`: handles OIDC discovery, redirect-based login, PKCE generation, code exchange, token parsing/validation, and token storage.
- `@angular/router`: routes the user to `/auth/login` and `/auth/callback` during the login flow.

## Client-side flow (what happens under the hood)

1. **User clicks Login**
   - In `src/app/auth/login/login.ts`, `this.oauthService.initLoginFlow()` is called.

2. **Library builds the authorization request and redirects the browser**
   - Reads config from `src/app/core/oidc/auth-code-pkce.config.ts` (issuer, clientId, scopes, redirect URI, `responseType: 'code'`).
   - Loads OIDC discovery from the issuer (via `/.well-known/openid-configuration`) to learn endpoints like `/oauth2/authorize` and `/oauth2/token`.
   - Generates PKCE values:
     - `code_verifier` (random secret)
     - `code_challenge = BASE64URL(SHA256(code_verifier))` with method `S256`
   - Redirects the browser to the Authorization Server `/authorize` endpoint with params like `client_id`, `redirect_uri`, `scope`, `state`, plus the PKCE `code_challenge`.

3. **User authenticates on the Authorization Server**
   - Credentials are entered on the Authorization Server’s login page (Spring Security form login).
   - After successful login, the Authorization Server redirects back to the SPA callback with `code` (and `state`).

4. **Callback route completes the login**
   - The route `'/auth/callback'` is configured in `src/app/app.routes.ts`.
   - In `src/app/auth/callback/callback.ts`, `await this.oauthService.tryLoginCodeFlow()` runs.
   - This call:
     - parses `code`/`state` from the callback URL
     - sends a back-channel POST to the Token Endpoint `/oauth2/token` with:
       - `grant_type=authorization_code`
       - `code`
       - `redirect_uri`
       - **PKCE `code_verifier`**
     - receives tokens (typically **ID Token** + **Access Token**, and sometimes Refresh Token depending on server settings)
     - stores tokens (default is browser storage used by the library) and updates the library’s auth state

5. **App proceeds as “logged in”**
   - After `tryLoginCodeFlow()` resolves, the SPA navigates back to `/`.
   - The app can read identity from the **ID Token** (who the user is) and use the **Access Token** to call APIs (not part of Level-0 yet).

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
