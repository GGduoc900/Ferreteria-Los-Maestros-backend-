package cl.ferreteria.notificaciones.config;

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
 * Enviar el formulario de contacto es público. Leer consultas e historial es de ADMIN. Registrar
 * correos/alertas lo hacen otros servicios (SERVICIO) o ADMIN.
 */
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwt) throws Exception {
        SeguridadBase.aplicar(http, jwt)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.POST, "/contacto")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/notificaciones/**")
                                        .hasAnyRole(ADMIN, SERVICIO)
                                        .requestMatchers("/contacto", "/contacto/**", "/notificaciones", "/notificaciones/**")
                                        .hasRole(ADMIN)
                                        .anyRequest()
                                        .authenticated());
        return http.build();
    }
}
