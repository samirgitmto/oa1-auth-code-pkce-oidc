package com.cred.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    /**
     * Temporary landing page for local development.
     * Helps avoid confusing 404s after successful form-login redirects to "/".
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        String authorize =
                "http://localhost:9000/oauth2/authorize"
                        + "?response_type=code"
                        + "&client_id=postman-client"
                        + "&redirect_uri=http://localhost:9999/callback"
                        + "&scope=openid%20read%20write"
                        + "&state=123";

        return """
                <!doctype html>
                <html>
                  <head>
                    <meta charset="utf-8" />
                    <title>auth-service</title>
                  </head>
                  <body>
                    <h3>auth-service is running</h3>
                    <p>This is a temporary landing page for local testing.</p>

                    <h4>Next steps</h4>
                    <ol>
                      <li>Open <a href="%s">authorize URL</a> in your browser</li>
                      <li>Login as <code>user1</code> / <code>password</code></li>
                      <li>Copy <code>code</code> from redirect URL</li>
                      <li>Exchange code in Postman: <code>POST /oauth2/token</code></li>
                    </ol>

                    <h4>Useful links</h4>
                    <ul>
                      <li><a href="/.well-known/openid-configuration">OIDC discovery</a></li>
                      <li><a href="/oauth2/jwks">JWKS</a></li>
                      <li><a href="/login">Login page</a></li>
                    </ul>
                  </body>
                </html>
                """.formatted(authorize);
    }
}
