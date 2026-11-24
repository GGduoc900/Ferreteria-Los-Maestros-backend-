package cl.ferreteria.notificaciones.dto;

import cl.ferreteria.notificaciones.model.Notificacion;

public record ContactoResponse(String mensaje, Notificacion consulta) {}
