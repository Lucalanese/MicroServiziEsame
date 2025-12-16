package it.unimol.exam_management_service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final PublicKey publicKey;

    public JwtAuthFilter(PublicKey jwtPublicKey) {
        this.publicKey = jwtPublicKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        /*try {
            String token = extractToken(request);
            if (token != null && validateToken(token)) {
                Claims claims = extractClaims(token);
                setupAuthentication(claims);
            }
        } catch (Exception e) {
            logger.error("Impossibile autenticare l'utente", e);
        }
*/
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Nel metodo validateToken di JwtAuthFilter
    private boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();

            // Verifica che il token non sia scaduto
            if (!expiration.after(new Date())) {
                return false;
            }

            // Opzionale: verifica che il token sia stato emesso per questo servizio
            /*String audience = claims.getAudience();
            if (audience == null || !audience.contains("exam-service")) {
                return false;
            }
*/
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private void setupAuthentication(Claims claims) {
        String userId = claims.getSubject();
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            role = "ADMINISTRATIVE";
        }
        // Trasforma il ruolo in formato ROLE_XXX (richiesto da Spring Security)
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

        // Crea l'oggetto principal (rappresenta l'utente autenticato)
        User principal = new User(username, "", Collections.singletonList(authority));

        // Crea l'oggetto Authentication e lo inserisce nel SecurityContext
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.singletonList(authority));

        // Memorizza l'ID utente come dettaglio aggiuntivo
        authentication.setDetails(userId);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}