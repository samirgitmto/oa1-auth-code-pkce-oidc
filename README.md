## OAuth/OIDC Learning Repo — High-level flow

This repo contains a working “Level 0” reference implementation of the **standard modern login flow**:

**OpenID Connect (OIDC) + OAuth 2.0 Authorization Code + PKCE (SPA client)**

### Components
- **Authorization Server (AS)**: `auth-service` (Spring Authorization Server + OIDC)
- **Client (SPA)**: `cred-auth-ui` (Angular + `angular-oauth2-oidc`)
- **Resource Server (API)**: not part of this level yet

### End-to-end flow (browser-based)
1. User opens the SPA (`cred-auth-ui`) and clicks **Login**
2. SPA redirects browser to Authorization Server `/oauth2/authorize`
   - includes `response_type=code`, `client_id`, `redirect_uri`, `scope`
   - includes PKCE `code_challenge` + `code_challenge_method=S256`
3. User authenticates on the Authorization Server login page (Spring Security form login)
4. Authorization Server redirects back to SPA callback:
   - `GET /auth/callback?code=...&state=...`
5. SPA exchanges the code for tokens at `/oauth2/token` (back-channel POST)
   - sends `code_verifier` (PKCE) + `code`
6. Authorization Server returns tokens:
   - **ID Token (OIDC)**: who the user is (meant for the client)
   - **Access Token (OAuth2)**: what the client can access (meant for APIs)

### Key learning outcomes (this level)
- OAuth vs OIDC: **access token** vs **id token**
- Why SPAs use **Authorization Code + PKCE**
- Where credentials go in this flow:
  - username/password are posted only to the **Authorization Server login**
  - the SPA never sends username/password to the token endpoint

### Local URLs (defaults)
- Authorization Server: `http://localhost:9000`
- SPA: `http://localhost:4200`
- Redirect URI: `http://localhost:4200/auth/callback`