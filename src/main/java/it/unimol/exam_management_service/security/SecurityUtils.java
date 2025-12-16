package it.unimol.exam_management_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * Ottiene l'ID dell'utente correntemente autenticato
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getDetails() != null) {
            return authentication.getDetails().toString();
        }
        return null;
    }

    /**
     * Verifica se l'utente corrente ha un ruolo specifico
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(authority -> authority.equals("ROLE_" + role.toUpperCase()));
        }
        return false;
    }

    /**
     * Verifica se l'utente corrente è un amministratore
     */
    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }

    /**
     * Verifica se l'utente corrente è un docente
     */
    public boolean isDocente() {
        return hasRole("DOCENTE");
    }

    /**
     * Verifica se l'utente corrente è uno studente
     */
    public boolean isStudente() {
        return hasRole("STUDENTE");
    }
    // Aggiungi questi metodi a SecurityUtils.java

    /**
     * Verifica se l'utente corrente è lo studente specificato o un amministratore
     */
    public boolean isStudentOrAdmin(Long studentId) {
        String currentUserId = getCurrentUserId();
        return (currentUserId != null && currentUserId.equals(studentId.toString())) || isAdmin();
    }

    /**
     * Verifica se l'utente corrente è il docente specificato o un amministratore
     */
    public boolean isProfessorOrAdmin(Long professorId) {
        String currentUserId = getCurrentUserId();
        return (currentUserId != null && currentUserId.equals(professorId.toString())) || isAdmin();
    }
}