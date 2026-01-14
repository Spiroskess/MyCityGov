package gr.hua.dit.mycitygov.config;

import gr.hua.dit.mycitygov.core.security.JwtAuthenticationFilter;
import gr.hua.dit.mycitygov.web.rest.error.RestApiAccessDeniedHandler;
import gr.hua.dit.mycitygov.web.rest.error.RestApiAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * API Security (JWT) - Stateless
     * Applies ONLY to: /api/** + swagger endpoints
     * Teacher-style: No redirects to "/" — returns JSON ApiError for 401/403.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      JwtAuthenticationFilter jwtFilter,
                                                      RestApiAuthenticationEntryPoint restApiAuthenticationEntryPoint,
                                                      RestApiAccessDeniedHandler restApiAccessDeniedHandler) throws Exception {

        http
            .securityMatcher("/api/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Auth endpoints public
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()

                // Optional public browsing for availability
                .requestMatchers(HttpMethod.GET, "/api/availability/**").permitAll()

                // Swagger / OpenAPI public
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // Όλα τα υπόλοιπα /api/** θέλουν JWT
                .anyRequest().authenticated()
            )

            // JWT filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // No form login/logout for API
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())

            //  JSON (ApiError) for 401/403
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(restApiAuthenticationEntryPoint)
                .accessDeniedHandler(restApiAccessDeniedHandler)
            );

        return http.build();
    }

    /**
     * UI Security - Session + Form Login (όπως το είχες)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login", "/register",
                    "/gov-token-login",
                    "/gov/**",
                    "/css/**", "/js/**",
                    "/h2-console/**"
                ).permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                .requestMatchers("/citizen/**").hasRole("CITIZEN")
                .anyRequest().authenticated()
            )

            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )

            .securityContext(sc -> sc
                .securityContextRepository(securityContextRepository())
                .requireExplicitSave(true)
            )

            .formLogin(form -> form
                .loginPage("/")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/?error=1")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=1")
                .permitAll()
            );

        return http.build();
    }
}
