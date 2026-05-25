package com.example.autoshop;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            @NonNull HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()

                        // Публичные страницы
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/products.html",
                                "/orders.html",
                                "/supplies.html",
                                "/brands.html",
                                "/categories.html",
                                "/suppliers.html",
                                "/warehouses.html",
                                "/users.html",
                                "/cart.html",
                                "/profile.html"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Публичные API
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/brands/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products/search"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/register"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products",
                                "/api/products/*/image",
                                "/api/categories/**",
                                "/api/brands/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/brands/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/brands/**"
                        ).hasRole("ADMIN")

                        // Пользователь
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/me"
                        ).hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/users/me"
                        ).hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/me/change-password"
                        ).hasAnyRole("USER", "ADMIN")

                        // Админ
                        .requestMatchers(
                                "/api/users/*/change-password",
                                "/api/supplies/**",
                                "/api/users/**",
                                "/api/warehouses/**",
                                "/api/suppliers/**"
                        ).hasRole("ADMIN")

                        // Заказы
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/**"
                        ).hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/orders"
                        ).hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/orders/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/orders/**"
                        ).hasRole("ADMIN")

                        // Всё остальное требует логин
                        .anyRequest().authenticated()
                )

                .httpBasic(basic -> basic
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Проверяем, если это внутренний редирект ошибки, отдаем статус ошибки
                            if (request.getAttribute("jakarta.servlet.error.status_code") != null) {
                                int statusCode = (int) request.getAttribute("jakarta.servlet.error.status_code");
                                response.setStatus(statusCode);
                            } else {
                                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            }
                            // Пишем текстовый ответ вместо отправки заголовка WWW-Authenticate
                            response.getWriter().write("Unauthorized: " + authException.getMessage());
                        })
                );


        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            @NonNull AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/error",
                "/favicon.ico",
                "/**/*.map",
                "/css/**",
                "/js/**",
                "/images/**"
        );
    }

}
