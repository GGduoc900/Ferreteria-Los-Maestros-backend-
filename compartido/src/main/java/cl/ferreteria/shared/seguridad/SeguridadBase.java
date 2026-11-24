package cl.ferreteria.shared.seguridad;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Reglas que comparten todos los servicios. Cada SecurityConfig llama a {@link #aplicar} y luego
 * agrega sus propias reglas por rol.
 *
 * <ul>
 *   <li>API sin estado (STATELESS): no hay sesión en el servidor, cada petición trae su JWT.
 *   <li>CSRF desactivado: no se usan cookies de sesión, el token viaja en el header Authorization.
 *   <li>Resource server JWT: lee {@code Authorization: Bearer <token>} y valida la firma.
 * </ul>
 */
public final class SeguridadBase {

    /** Rutas abiertas en todos los servicios: estado y documentación Swagger. */
    public static final String[] RUTAS_PUBLICAS = {
        "/estado", "/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**"
    };

    public static final String ADMIN = "ADMIN";
    public static final String CLIENTE = "CLIENTE";
    public static final String SERVICIO = "SERVICIO";

    private SeguridadBase() {}

    public static HttpSecurity aplicar(HttpSecurity http, JwtAuthenticationConverter converter)
            throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)))
                .authorizeHttpRequests(
                        a ->
                                a.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(RUTAS_PUBLICAS)
                                        .permitAll());
    }
}
