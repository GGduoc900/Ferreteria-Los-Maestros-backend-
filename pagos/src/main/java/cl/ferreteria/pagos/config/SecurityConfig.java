package cl.ferreteria.pagos.config;

import static cl.ferreteria.shared.seguridad.SeguridadBase.ADMIN;

import cl.ferreteria.shared.seguridad.SeguridadBase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/** Pagos: métodos de pago públicos; pagar requiere sesión; confirmar pagos es de ADMIN. */
@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwt) throws Exception {
        SeguridadBase.aplicar(http, jwt)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(HttpMethod.GET, "/pagos/metodos-pago")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.PUT, "/pagos/*/estado")
                                        .hasRole(ADMIN)
                                        .anyRequest()
                                        .authenticated());
        return http.build();
    }
}
