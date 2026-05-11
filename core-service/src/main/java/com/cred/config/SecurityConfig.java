package com.cred.config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // DO NOT REMOVE IT. Only for reference.
    //  @Bean
    SecurityFilterChain securityFilterChain0(HttpSecurity http) throws Exception {
        http
                // Resource servers are stateless APIs (Bearer token per request)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/products").hasAuthority("SCOPE_read")
//                        .requestMatchers(HttpMethod.POST, "/products").hasAuthority("SCOPE_write")
                        .anyRequest().authenticated()
                );
                // Enable JWT validation for Authorization: Bearer <token>
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    /**
     * Maps OAuth scopes to {@code SCOPE_*} authorities and adds Spring roles from JWT {@code sub}
     * so {@code @PreAuthorize("hasRole('ADMIN')")} works with auth-service in-memory users ({@code user1} / {@code admin1}).
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));
            String sub = jwt.getSubject();
            if ("admin1".equals(sub)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else if ("user1".equals(sub)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            return authorities;
        });
        return converter;
    }

}
