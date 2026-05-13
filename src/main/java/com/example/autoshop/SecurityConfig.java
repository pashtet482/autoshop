package com.example.autoshop;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/",
                "/index.html",
                "/login.html",
                "/products.html",
                "/orders.html",
                "/supplies.html",
                "/warehouses.html",
                "/users.html",
                "/cart.html",
                "/profile.html",
                "/css/**",
                "/js/**"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            @NonNull HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/",
                                "/index.html",
                                "/login.html",
                                "/products.html",
                                "/orders.html",
                                "/supplies.html",
                                "/warehouses.html",
                                "/users.html",
                                "/cart.html",
                                "/profile.html",
                                "/css/**",
                                "/js/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/brands/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/products/search"
                        ).permitAll()
                        .requestMatchers(
                                "/api/users/*/change-password"
                        ).hasAnyRole("USER", "ADMIN")
                        .requestMatchers(
                                "/api/users/me"
                        ).hasAnyRole("USER", "ADMIN")
                        .requestMatchers(
                                "/api/supplies/**",
                                "/api/users/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/orders/**"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        .anyRequest()
                        .authenticated()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            @NonNull AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }
}
