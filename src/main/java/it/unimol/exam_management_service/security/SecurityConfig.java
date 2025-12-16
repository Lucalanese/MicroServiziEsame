package it.unimol.exam_management_service.security;

import it.unimol.exam_management_service.security.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disabilita CSRF per le API RESTful
                .csrf(csrf -> csrf.disable())

                // Configurazione CORS
                .cors(cors -> cors.configure(http))

                // Gestione della sessione
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Gestione delle eccezioni di autenticazione
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // Configurazione delle autorizzazioni
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoint pubblici (non richiedono autenticazione)
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/actuator/**").permitAll()
                        .requestMatchers("/api/v1/exams/calendar").permitAll()

                        // Consenti accesso a chi ha ruolo ADMINISTRATIVE o ADMIN (convertito da "admin")
                        .requestMatchers("/api/v1/exams/**").hasAnyAuthority("ROLE_ADMINISTRATIVE", "ROLE_ADMIN")
                        .requestMatchers("/api/v1/enrollments/**").hasAnyAuthority("ROLE_ADMINISTRATIVE", "ROLE_ADMIN")
                        .requestMatchers("/api/v1/grades/**").hasAnyAuthority("ROLE_ADMINISTRATIVE", "ROLE_ADMIN")

                        // Il resto richiede solo autenticazione, senza ruoli specifici
                        .anyRequest().authenticated()
                );

        // Aggiungi il filtro JWT prima del filtro di autenticazione standard
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}