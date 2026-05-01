package com.cred.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/**
	 * OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
	 * It automatically configures endpoints like:
	 * /oauth2/authorize	 * /oauth2/token	 * /oauth2/jwks	 * /oauth2/introspect	 * /oauth2/revoke
	 * Basically: the OAuth server is born here.
	 * 
	 * .oidc(Customizer.withDefaults());
	 * This enables OpenID Connect features:
	 * /.well-known/openid-configuration	 * /userinfo	 * ID tokens
	 * Without this, you only have OAuth (authorization), not identity.
	 * 
	 * @param http
	 * @return
	 * @throws Exception
	 */
    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults()); // enables OIDC endpoints (/.well-known, userinfo, etc.)

        http.cors(Customizer.withDefaults())
        	.exceptionHandling(exceptions ->
        	        exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
        	);

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    UserDetailsService users(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("user1")
                .password(passwordEncoder.encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin1")
                .password(passwordEncoder.encode("password"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        RegisteredClient postmanClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("postman-client")
                .clientSecret(passwordEncoder.encode("postman-secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:9999/callback")
                .scope(OidcScopes.OPENID)
                .scope("read")
                .scope("write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .build())
                .build();
        
        RegisteredClient spaClient = RegisteredClient.withId(UUID.randomUUID().toString())
        		.clientId("cred-auth-ui")
        		.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
        		.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        		.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
        		.redirectUri("http://localhost:4200/auth/callback")
        		.scope(OidcScopes.OPENID)
        		.scope(OidcScopes.PROFILE)
        		.scope(OidcScopes.EMAIL)
        		.scope("read")
        		.scope("write")
        		.tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
//                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)  // Requires PKCE for this client
//                        .requireAuthorizationConsent(true)  // Optional: forces consent screen
                        .requireAuthorizationConsent(false)
                        .build())
                .build();
        		

        return new InMemoryRegisteredClientRepository(postmanClient, spaClient);
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings() {
        // Keeps defaults: /oauth2/authorize, /oauth2/token, /oauth2/jwks, /.well-known/openid-configuration, etc.
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration cfg = new CorsConfiguration();
      cfg.setAllowedOrigins(List.of("http://localhost:4200"));
      cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
      cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
      cfg.setAllowCredentials(true); // needed for JSESSIONID-based login flow
      cfg.setMaxAge(3600L);

      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", cfg);
      return source;
    }
    
    @Bean
    JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }
    
    private static RSAKey generateRsa() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }
}

