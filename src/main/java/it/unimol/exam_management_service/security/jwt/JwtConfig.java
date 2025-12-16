package it.unimol.exam_management_service.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {
    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @Value("${jwt.public-key:}")
    private String publicKeyBase64;

    @Bean
    public PublicKey jwtPublicKey() throws Exception {
        try {
            // Rimuovi eventuali caratteri non validi per Base64
            String cleanKey = publicKeyBase64
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            logger.debug("Chiave pulita: {}", cleanKey);

            // Usa getMimeDecoder invece di getDecoder
            byte[] publicKeyBytes = Base64.getMimeDecoder().decode(cleanKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            logger.error("Errore durante il parsing della chiave pubblica JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Impossibile decodificare la chiave pubblica JWT. Verifica che sia una chiave RSA valida in formato Base64.", e);
        }
    }

}