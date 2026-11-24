package cl.ferreteria.notificaciones.service;

import cl.ferreteria.notificaciones.dto.AlertaStockRequest;
import cl.ferreteria.notificaciones.dto.AlertaStockResponse;
import cl.ferreteria.notificaciones.dto.ContactoRequest;
import cl.ferreteria.notificaciones.dto.CorreoRequest;
import cl.ferreteria.notificaciones.model.Notificacion;
import cl.ferreteria.notificaciones.model.TipoNotificacion;
import cl.ferreteria.notificaciones.repository.NotificacionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/** Formulario de contacto (público) e historial de correos (simulados). */
@Service
@Transactional(readOnly = true)
public class NotificacionService {
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final NotificacionRepository repo;

    public NotificacionService(NotificacionRepository repo) {
        this.repo = repo;
    }

    /** Mismas reglas que valida el formulario de React: nombre ≤ 100, mensaje ≤ 500, correo opcional. */
    @Transactional
    public Notificacion registrarContacto(ContactoRequest r) {
        String nombre = texto(r.nombre());
        String mensaje = texto(r.mensaje());
        String correo = texto(r.correo());
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (nombre.length() > 100) throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres");
        if (mensaje.isEmpty()) throw new IllegalArgumentException("El mensaje es obligatorio");
        if (mensaje.length() > 500) throw new IllegalArgumentException("El mensaje no puede superar los 500 caracteres");
        if (!correo.isEmpty() && !CORREO.matcher(correo).matches()) throw new IllegalArgumentException("Ingresa un correo válido");

        Notificacion n = new Notificacion();
        n.setTipo(TipoNotificacion.CONTACTO);
        n.setNombre(nombre);
        n.setCorreo(correo.isEmpty() ? null : correo);
        n.setTelefono(texto(r.telefono()).isEmpty() ? null : texto(r.telefono()));
        n.setAsunto(texto(r.asunto()).isEmpty() ? "Consulta desde el sitio web" : texto(r.asunto()));
        n.setMensaje(mensaje);
        n.setEstado("RECIBIDA");
        n.setFecha(Instant.now());
        return repo.save(n);
    }

    public List<Notificacion> listarContactos() {
        return repo.findByTipoOrderByFechaDesc(TipoNotificacion.CONTACTO);
    }

    @Transactional
    public Notificacion marcarContacto(Long id, String estado) {
        Notificacion n = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Consulta no encontrada"));
        String nuevo = texto(estado).toUpperCase();
        if (!List.of("RECIBIDA", "LEIDA", "RESPONDIDA").contains(nuevo)) {
            throw new IllegalArgumentException("Estado no válido: RECIBIDA, LEIDA o RESPONDIDA");
        }
        n.setEstado(nuevo);
        return repo.save(n);
    }

    @Transactional
    public Notificacion enviarCorreo(CorreoRequest r) {
        if (texto(r.para()).isEmpty() || texto(r.mensaje()).isEmpty()) {
            throw new IllegalArgumentException("Faltan los campos para y mensaje");
        }
        Notificacion n = new Notificacion();
        n.setTipo(TipoNotificacion.CORREO);
        n.setPara(r.para());
        n.setAsunto(r.asunto());
        n.setMensaje(r.mensaje());
        n.setEstado("SIMULADA");
        n.setFecha(Instant.now());
        return repo.save(n);
    }

    public List<Notificacion> listar() {
        return repo.findAllByOrderByFechaDesc();
    }

    public AlertaStockResponse alertaStock(AlertaStockRequest request) {
        return new AlertaStockResponse("ALERTA_STOCK", request.producto(), "SIMULADA");
    }

    private static String texto(String s) {
        return s == null ? "" : s.trim();
    }
}
