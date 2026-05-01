# ma7: oa1 benchmark snapshot (what’s done + what’s left)

This file is a **benchmark snapshot** for `oa1-auth-code-pkce-oidc` so you can compare future levels (or Keycloak swap) against a known-good baseline.

It documents:

- what we have already built in **`auth-service`** (Authorization Server + OIDC)
- what we have already built in **`cred-auth-ui`** (SPA client using auth code + PKCE)
- what is left to do per the oa1 plan in earlier `m-auth docs`
- how “app endpoints” like `/` and `/hello` are currently accessed (session cookie), vs how OAuth endpoints behave

---

## 1) What we have achieved so far

### A) `auth-service` (OAuth2 Authorization Server + OIDC)

Implemented using **Spring Authorization Server** with **OIDC enabled**.

**Capabilities already present:**

- **OAuth2 / OIDC endpoints exist**
  - Authorization endpoint: `GET /oauth2/authorize`
  - Token endpoint: `POST /oauth2/token`
  - JWKS: `GET /oauth2/jwks`
  - OIDC discovery: `GET /.well-known/openid-configuration`
- **Clients registered in-memory**
  - `postman-client` (confidential) for Postman-first testing
  - `cred-auth-ui` (public SPA) with **PKCE required**
- **Users in-memory + interactive login available**
  - Spring Security `formLogin()` is enabled, so browser login works
- **JWT signing keys published via JWKS**
  - RSA keypair generated at startup (learning convenience)
  - resource servers (later) can validate JWT signatures using `/oauth2/jwks`

### B) `cred-auth-ui` (SPA client)

Angular SPA configured as an **OIDC client** using **Authorization Code + PKCE**.

**Capabilities already present:**

- Uses library: **`angular-oauth2-oidc`**
- Client config points to issuer `http://localhost:9000`, response type `code`, and redirect URI `/auth/callback`
- Login trigger:
  - `OAuthService.initLoginFlow()` → redirects browser to `/oauth2/authorize` (PKCE under the hood)
- Callback handler:
  - `OAuthService.tryLoginCodeFlow()` → parses `code` and exchanges it at `/oauth2/token` (with `code_verifier`)

---

## 2) “Two groups of endpoints” and how session (`JSESSIONID`) applies

In `auth-service`, there are effectively **two groups of endpoints** because there are **two `SecurityFilterChain`s**.

### Group A: Authorization Server endpoints (`/oauth2/**`, `/.well-known/**`)

Examples:

- `/oauth2/authorize`
- `/.well-known/openid-configuration`
- `/oauth2/token`
- `/oauth2/jwks`

**How session fits here:**

- The **interactive** endpoint `/oauth2/authorize` is part of a browser redirect flow and commonly uses the Authorization Server’s **session cookie (`JSESSIONID`)**.
  - If a browser already has a valid `JSESSIONID`, the Authorization Server knows the user is logged in and typically won’t prompt for login again.
- The **token endpoint** `/oauth2/token` does **not rely on session**.
  - It is meant to be called with the **authorization code** (and **PKCE** for public clients like SPAs) and the client authentication rules.

**Key idea:** Session helps with browser login + staying logged in across redirects, but **OAuth token issuance is still driven by code+PKCE**, not “sessionid gives you tokens”.

### Group B: App endpoints (regular controllers)

These are handled by the app security chain (the “non-authorization-server” chain).

Examples:

- `/` (landing page)
- `/hello`
- `/actuator/**` (explicitly permitted in current config)

**How session applies:**

- Because `formLogin()` is enabled, these endpoints can be accessed using the same **`JSESSIONID`** once the user logs in.
- This is typical Spring Security “web app session auth” behavior.

---

## 3) Benchmark note: endpoints we discussed (`HomeController`, `CredController`)

These endpoints are **not OAuth endpoints** and are currently best thought of as “app endpoints”.

### `HomeController`

- `GET /`
- Currently returns an HTML landing page with:
  - a pre-built `/oauth2/authorize` link (for Postman-client testing)
  - links to discovery, JWKS, and login page
- In current security config, `/` is **permitAll** (public).

### `CredController`

- `GET /hello`
- In current security config, it is protected by:
  - `anyRequest().authenticated()`
- It is currently accessed via **session cookie**:
  - login via `/login` → receive `JSESSIONID` → call `/hello` with that cookie
- It is **not** currently designed to be accessed via `Authorization: Bearer <access_token>`.

---

## 4) What is left to do in oa1 (based on earlier plan docs)

This repo is “Level 0 / oa1 baseline” for **Auth Code + PKCE + OIDC**. What remains (as future work for the larger real-world plan) is mainly outside this module pair:

- **Add Resource Server(s)** (Service A / Service B)
  - validate JWT access tokens using issuer + JWKS
  - enforce authz using scopes/roles (`read`, `write`, roles, etc.)
- **Add inter-service call scenario**
  - Service A calls Service B and propagates `Authorization: Bearer <access_token>`
- **Add API Gateway (optional in later level)**
  - route to services, handle CORS, optionally validate tokens at the edge
- **Hardening / lifecycle (later)**
  - refresh token policies, revocation/rotation, better issuer configuration, persistence (DB) for clients/users/consents
- **Keycloak comparison (later milestone)**
  - swap auth-service with Keycloak and compare discovery, claims, refresh behavior, and configuration

---

## 5) “Done” definition for this benchmark

This benchmark is considered valid if:

- You can complete **Authorization Code flow** (SPA or manual browser) and obtain tokens via `/oauth2/token`
- `/.well-known/openid-configuration` and `/oauth2/jwks` are reachable
- PKCE is enforced for the SPA client (`cred-auth-ui`)
- `/hello` is accessible after login using `JSESSIONID` (session-based app endpoint)
