package cl.ferreteria.notificaciones.dto;

public record ContactoRequest(
        String nombre, String correo, String telefono, String asunto, String mensaje) {}
