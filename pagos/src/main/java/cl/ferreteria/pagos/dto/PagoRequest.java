package cl.ferreteria.pagos.dto;

public record PagoRequest(Long idVenta, String metodo, Integer monto, String referencia) {}
