package cl.ferreteria.shared.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación OpenAPI/Swagger de cada microservicio: http://localhost:300X/swagger-ui.html
 *
 * <p>Declara el esquema "bearerAuth" para que el botón <b>Authorize</b> de Swagger UI permita pegar
 * el JWT obtenido en {@code POST /autenticacion/inicio-sesion} (servicio usuarios, puerto 3005).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI documentacion(@Value("${spring.application.name}") String servicio) {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Ferretería Los Maestros – API de " + servicio)
                                .version("1.0.0")
                                .description(
                                        "Microservicio de " + servicio + ". Para endpoints protegidos: "
                                                + "inicia sesión en el servicio usuarios (3005) y pega el token en Authorize.")
                                .contact(new Contact().name("Ferretería Los Maestros").email("contacto@losmaestros.cl")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")));
    }
}
