package com.rice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AppUserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Value("${app.cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedOriginPatterns(List.of(allowedOriginPatterns.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/brands/**", "/api/settings").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/coupons", "/api/banners").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/coupons/validate").permitAll()
                        // Staff management (inviting/editing/deleting other admin & employee
                        // accounts, and their permission grants) is ADMIN-only, full stop - an
                        // employee is never allowed to manage other staff regardless of any
                        // permission flag. This narrower matcher must come before the general
                        // /api/admin/** one below (Spring evaluates in order, first match wins).
                        .requestMatchers("/api/admin/staff/**").hasRole("ADMIN")
                        // Every other /api/admin/** endpoint is reachable by ADMIN or EMPLOYEE at
                        // the filter level - the actual per-module gate (Products/Orders/
                        // Customers/Coupons/Delivery&Tax) happens via @PreAuthorize + PermissionCheck
                        // on each controller method, not here. Without this line an employee's
                        // token clears login fine but every subsequent /api/admin/** call 401s,
                        // which is exactly the bug this fixes.
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/staff/my-permissions").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Authentication required\"}");
                        })
                        // Without this, a filter-level hasRole()/hasAnyRole() mismatch (e.g. an
                        // EMPLOYEE hitting the ADMIN-only /api/admin/staff/** matcher) has no
                        // accessDeniedHandler to catch it, so Spring's ExceptionTranslationFilter
                        // falls back to the authenticationEntryPoint above and returns 401 - even
                        // though the caller IS authenticated, just under-privileged for this one
                        // route. The frontend treats any 401 as "token is dead" and force-logs-out,
                        // so a fully-valid employee session got kicked out just from loading a page
                        // (Customers) that also happens to call an admin-only endpoint in the
                        // background. 403 here leaves the session alone, matching how @PreAuthorize
                        // denials on individual controller methods already behave.
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"You don't have permission to do this\"}");
                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
