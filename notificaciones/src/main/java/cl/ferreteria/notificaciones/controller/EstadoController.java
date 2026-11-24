package cl.ferreteria.notificaciones.controller;

import cl.ferreteria.shared.EstadoBaseController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class EstadoController extends EstadoBaseController {
    @GetMapping("/estado")
    public Map<String, Object> estado() {
        return estado("Notificaciones", "Microservicio de notificaciones");
    }
}
