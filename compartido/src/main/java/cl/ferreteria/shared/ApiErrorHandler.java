package cl.ferreteria.shared;

import cl.ferreteria.shared.web.ServicioNoDisponibleException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Traduce las excepciones de la lógica de negocio a respuestas HTTP con un JSON uniforme:
 * {@code { "error": "...", "estado": 404, "fecha": "..." }}.
 *
 * <ul>
 *   <li>NoSuchElementException → 404 (no existe)
 *   <li>IllegalArgumentException / JSON mal formado → 400 (datos inválidos)
 *   <li>IllegalStateException → 409 (conflicto con el estado actual, ej. código repetido)
 *   <li>AccessDeniedException → 403
 *   <li>ServicioNoDisponibleException → 503 (otro microservicio no responde)
 * </ul>
 */
@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(NoSuchElementException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(IllegalArgumentException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> conflicto(IllegalStateException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> sinPermiso(AccessDeniedException e) {
        return respuesta(HttpStatus.FORBIDDEN, "No tienes permisos para esta operación");
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> servicioCaido(ServicioNoDisponibleException e) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> conEstado(ResponseStatusException e) {
        return respuesta(e.getStatusCode(), e.getReason());
    }

    /** Errores propios de Spring MVC (404 de ruta, 405, parámetro faltante...) conservan su código. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> otro(Exception e) {
        if (e instanceof ErrorResponse er) {
            String detalle = er.getBody().getDetail();
            return respuesta(er.getStatusCode(), detalle != null ? detalle : e.getMessage());
        }
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatusCode estado, String mensaje) {
        var cuerpo = new LinkedHashMap<String, Object>();
        cuerpo.put("error", mensaje == null || mensaje.isBlank() ? "Error" : mensaje);
        cuerpo.put("estado", estado.value());
        cuerpo.put("fecha", Instant.now().toString());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
