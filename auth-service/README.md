## auth-service (OAuth2 Authorization Server + OIDC) — Learning Project

This Spring Boot app is an **OAuth 2.0 Authorization Server** implemented using **Spring Authorization Server**, with **OpenID Connect (OIDC)** enabled.

It is designed to be used with the SPA client in `../cred-auth-ui` which uses **Authorization Code + PKCE**.

---

## What this service is in OAuth terms

- **Authorization Server (AS)**: this app (`auth-service`)
- **Client**: the SPA (`cred-auth-ui`)
- **Resource Server**: not part of Level-0 yet (will come later)

This project focuses on the most standard modern browser login pattern:
**OIDC + Authorization Code grant + PKCE (for public clients / SPAs).**

---

## The high-level flow (Auth Code + PKCE + OIDC)

1. User opens SPA and clicks **Login**
2. SPA redirects the browser to the Authorization Server’s **Authorization Endpoint**
   - request includes `response_type=code`, `client_id`, `redirect_uri`, `scope`
   - and PKCE params: `code_challenge`, `code_challenge_method=S256`
3. User authenticates on the Authorization Server login page (Spring Security **form login**)
4. Authorization Server redirects back to SPA callback:
   - `GET http://localhost:4200/auth/callback?code=...&state=...`
5. SPA calls the Authorization Server **Token Endpoint** to exchange code for tokens:
   - sends `grant_type=authorization_code`, `code`, `redirect_uri`
   - and the PKCE secret: `code_verifier`
6. Authorization Server validates PKCE and returns tokens:
   - **ID Token** (OIDC identity)
   - **Access Token** (OAuth authorization for APIs)
   - (Refresh Token may be present depending on settings)

Important: the SPA never sends username/password to `/oauth2/token`.
Credentials are only posted to the Authorization Server login endpoint during interactive login.

---

## Where the implementation lives

Core configuration is in:

- `src/main/java/com/cred/config/SecurityConfig.java`

Key things happening there:

- **Authorization Server enabled**
  - `OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)`
  - exposes endpoints like `/oauth2/authorize`, `/oauth2/token`, `/oauth2/jwks`, etc.

- **OIDC enabled**
  - `.oidc(Customizer.withDefaults())`
  - adds discovery (`/.well-known/openid-configuration`), ID Tokens, userinfo (where applicable)

- **Client registration**
  - Registers `cred-auth-ui` as a **public client**
  - Uses **Authorization Code** and enables **Refresh Token** grant type
  - Configures SPA redirect URI: `http://localhost:4200/auth/callback`

- **PKCE enforced**
  - `ClientSettings.requireProofKey(true)`
  - this is the server-side “PKCE is mandatory for this client” switch

- **Test users**
  - In-memory users exist (for learning)
  - Form login is enabled so you can authenticate interactively

- **Signing keys (JWKS)**
  - JWK is generated in-memory on startup (learning convenience)
  - Tokens are signed; public keys are served via JWKS endpoint

---

## Critical learning points (Level-0)

- **OAuth vs OIDC**
  - OAuth Access Token answers: “what can the client access?”
  - OIDC ID Token answers: “who is the user?”

- **Why Authorization Code (not implicit)**
  - Code flow keeps tokens off the front-channel redirect.
  - Token exchange happens on the back-channel (SPA → token endpoint).

- **Why PKCE exists**
  - SPAs are **public clients** (no safe client secret).
  - PKCE binds the `code` to the client instance that started login.
  - If an attacker steals the `code`, they still can’t redeem it without `code_verifier`.

- **Form login is not “password grant”**
  - Form login is interactive authentication at the Authorization Server.
  - Password grant is non-interactive credential forwarding to the token endpoint (`grant_type=password`) — not what we do here.

- **Discovery matters**
  - OIDC discovery (`/.well-known/openid-configuration`) is how clients learn the right endpoints and capabilities.

---

## Local ports (default)

- Authorization Server: `http://localhost:9000`
- SPA redirect URI: `http://localhost:4200/auth/callback`

---

## Suggested self-check questions

If you can answer these, Level-0 is solid:

- What is the difference between **access token** and **id token**?
- What exactly is the **authorization code** and why is it short-lived?
- What are **code_verifier** and **code_challenge** and where are they used?
- Why must a SPA use **PKCE**?
- Where do credentials go in this flow, and where do they *not* go?