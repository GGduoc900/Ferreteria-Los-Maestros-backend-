package cl.ferreteria.pagos.dto;

public record ResultadoValidacion(boolean valida, String estadoResultante, Integer montoEsperado) {}
