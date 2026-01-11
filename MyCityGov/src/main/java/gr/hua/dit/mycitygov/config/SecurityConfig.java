package gr.hua.dit.mycitygov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Κρυπτογράφηση κωδικών χρηστών (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager για login με username/password
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Αποθήκευση SecurityContext στο session (cookie-based UI security)
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Public σελίδες και assets
                .requestMatchers(
                    "/", "/login", "/register",
                    "/gov-token-login",
                    "/gov/**",
                    "/css/**", "/js/**",
                    "/h2-console/**"
                ).permitAll()

                // Role-based πρόσβαση στο UI
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                .requestMatchers("/citizen/**").hasRole("CITIZEN")
                .anyRequest().authenticated()
            )

            // Για να λειτουργεί το H2 console σε iframe
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // Session-based security context (χρησιμοποιείται και από το manual login service σου)
            .securityContext(sc -> sc
                .securityContextRepository(securityContextRepository())
                .requireExplicitSave(true)
            )

            // Form login για το UI
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
