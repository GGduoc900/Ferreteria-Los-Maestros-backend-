package cl.ferreteria.ventas.config;

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
 * Ventas: todo requiere sesión. Cambiar el estado del pedido es de ADMIN (o del servicio de pagos
 * cuando aprueba un pago). Que un CLIENTE vea solo sus ventas se controla en VentaService.
 */
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwt) throws Exception {
        SeguridadBase.aplicar(http, jwt)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.PUT, "/ventas/*/estado")
                                        .hasAnyRole(ADMIN, SERVICIO)
                                        .anyRequest()
                                        .authenticated());
        return http.build();
    }
}
