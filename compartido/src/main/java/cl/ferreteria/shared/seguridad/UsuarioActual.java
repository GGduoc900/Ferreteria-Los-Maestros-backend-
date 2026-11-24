package cl.ferreteria.shared.seguridad;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Datos del usuario autenticado sacados del JWT, para pasarlos a la capa de servicio sin que esta
 * dependa de clases HTTP/Spring Security.
 */
public record UsuarioActual(String correo, String rol) {

    public static UsuarioActual de(Jwt jwt) {
        return new UsuarioActual(jwt.getSubject(), jwt.getClaimAsString(JwtConfig.CLAIM_ROL));
    }

    public boolean esAdmin() {
        return SeguridadBase.ADMIN.equals(rol);
    }

    public boolean esServicio() {
        return SeguridadBase.SERVICIO.equals(rol);
    }

    /** ADMIN y los servicios internos ven todo; un CLIENTE solo lo suyo. */
    public boolean puedeVerTodo() {
        return esAdmin() || esServicio();
    }
}
