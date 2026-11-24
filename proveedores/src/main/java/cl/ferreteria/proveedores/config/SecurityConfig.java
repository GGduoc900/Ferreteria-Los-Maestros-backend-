package cl.ferreteria.proveedores.config;

import static cl.ferreteria.shared.seguridad.SeguridadBase.ADMIN;

import cl.ferreteria.shared.seguridad.SeguridadBase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/** Proveedores y órdenes de compra: gestión interna, solo ADMIN. */
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwt) throws Exception {
        SeguridadBase.aplicar(http, jwt).authorizeHttpRequests(auth -> auth.anyRequest().hasRole(ADMIN));
        return http.build();
    }
}
