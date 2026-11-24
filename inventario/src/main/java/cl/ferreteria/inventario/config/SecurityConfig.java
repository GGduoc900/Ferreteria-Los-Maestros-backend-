package cl.ferreteria.inventario.config;

import static cl.ferreteria.shared.seguridad.SeguridadBase.ADMIN;
import static cl.ferreteria.shared.seguridad.SeguridadBase.SERVICIO;

import cl.ferreteria.shared.seguridad.SeguridadBase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Inventario expone precios de compra y stock exacto, por eso no tiene rutas públicas:
 *
 * <ul>
 *   <li>Lectura: ADMIN o SERVICIO (catálogo consulta aquí para armar la vitrina pública).
 *   <li>Crear, editar, eliminar y ajustar stock: solo ADMIN.
 *   <li>/stock/descontar y /stock/reponer: SERVICIO (ventas) o ADMIN.
 * </ul>
 */
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwt) throws Exception {
        SeguridadBase.aplicar(http, jwt)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.GET, "/productos", "/productos/**")
                                        .hasAnyRole(ADMIN, SERVICIO)
                                        .requestMatchers("/stock/descontar", "/stock/reponer")
                                        .hasAnyRole(ADMIN, SERVICIO)
                                        .requestMatchers("/productos", "/productos/**", "/stock/**")
                                        .hasRole(ADMIN)
                                        .anyRequest()
                                        .authenticated());
        return http.build();
    }
}
