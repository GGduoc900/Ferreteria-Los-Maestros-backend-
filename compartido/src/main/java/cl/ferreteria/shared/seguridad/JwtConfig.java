package cl.ferreteria.shared.seguridad;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Configuración JWT común a los 7 microservicios.
 *
 * <p>Los tokens se firman con HMAC-SHA256 usando una clave compartida ({@code jwt.secreto}).
 * El microservicio de usuarios los emite al iniciar sesión y todos los demás los validan de forma
 * local (firma, expiración y emisor), sin tener que llamar a usuarios en cada petición.
 *
 * <p>El claim {@code rol} (CLIENTE, ADMIN o SERVICIO) se transforma en la autoridad {@code ROLE_<rol>},
 * que es la que se revisa con {@code hasRole(...)} en cada SecurityConfig.
 */
@Configuration
public class JwtConfig {

    public static final String CLAIM_ROL = "rol";

    @Bean
    SecretKey claveJwt(@Value("${jwt.secreto}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("jwt.secreto debe tener al menos 32 caracteres (HS256)");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey claveJwt, @Value("${jwt.emisor}") String emisor) {
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(claveJwt).macAlgorithm(MacAlgorithm.HS256).build();
        // Valida expiración (exp/nbf) y que lo haya emitido nuestro sistema (iss)
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(emisor));
        return decoder;
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(CLAIM_ROL);
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    /** Permite que el frontend React (Vite) llame a la API desde otro origen. */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${cors.origenes}") List<String> origenes) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origenes);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
