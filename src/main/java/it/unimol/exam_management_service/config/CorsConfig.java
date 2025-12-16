package it.unimol.exam_management_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Consenti le credenziali come i cookie
        config.setAllowCredentials(true);

        // Origini consentite - modificare in base alle tue esigenze
        config.addAllowedOrigin("http://localhost:3000"); // Frontend dev
        config.addAllowedOrigin("https://tuo-dominio-produzione.it"); // Produzione

        // Metodi HTTP consentiti
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Header consentiti
        config.addAllowedHeader("*");

        // Esponi l'header Authorization nella risposta
        config.addExposedHeader("Authorization");

        // Imposta la durata della cache per le richieste preflight OPTIONS
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}