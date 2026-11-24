package cl.ferreteria.notificaciones.controller;

import cl.ferreteria.notificaciones.dto.AlertaStockRequest;
import cl.ferreteria.notificaciones.dto.AlertaStockResponse;
import cl.ferreteria.notificaciones.dto.ContactoRequest;
import cl.ferreteria.notificaciones.dto.ContactoResponse;
import cl.ferreteria.notificaciones.dto.CorreoRequest;
import cl.ferreteria.notificaciones.dto.EstadoContactoRequest;
import cl.ferreteria.notificaciones.model.Notificacion;
import cl.ferreteria.notificaciones.service.NotificacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Contacto y notificaciones")
public class NotificacionController {
    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @PostMapping("/contacto")
    @Operation(summary = "Enviar el formulario de contacto (público)")
    public ResponseEntity<ContactoResponse> contacto(@RequestBody ContactoRequest body) {
        Notificacion consulta = service.registrarContacto(body);
        return ResponseEntity.status(201).body(new ContactoResponse("Consulta recibida", consulta));
    }

    @GetMapping("/contacto")
    @Operation(summary = "Consultas recibidas (ADMIN)")
    public List<Notificacion> contactos() {
        return service.listarContactos();
    }

    @PutMapping("/contacto/{id}")
    @Operation(summary = "Marcar consulta como LEIDA o RESPONDIDA (ADMIN)")
    public Notificacion marcar(@PathVariable Long id, @RequestBody EstadoContactoRequest body) {
        return service.marcarContacto(id, body.estado());
    }

    @PostMapping("/notificaciones/correo")
    @Operation(summary = "Registrar un correo (simulado) — uso interno")
    public ResponseEntity<Notificacion> correo(@RequestBody CorreoRequest body) {
        return ResponseEntity.status(201).body(service.enviarCorreo(body));
    }

    @GetMapping("/notificaciones")
    @Operation(summary = "Historial de notificaciones (ADMIN)")
    public List<Notificacion> notificaciones() {
        return service.listar();
    }

    @PostMapping("/notificaciones/alerta-stock")
    @Operation(summary = "Alerta de stock bajo — uso interno")
    public ResponseEntity<AlertaStockResponse> alertaStock(@RequestBody AlertaStockRequest body) {
        return ResponseEntity.status(201).body(service.alertaStock(body));
    }
}
