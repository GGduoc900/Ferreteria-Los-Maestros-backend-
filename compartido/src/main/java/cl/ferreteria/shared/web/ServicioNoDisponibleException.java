package cl.ferreteria.shared.web;

/** Otro microservicio no respondió a tiempo o está apagado → 503. */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
