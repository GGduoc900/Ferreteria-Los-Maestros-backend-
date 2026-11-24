package cl.ferreteria.pagos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Los datos de la venta que pagos necesita (respuesta de GET /ventas/{id} en ventas). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VentaRemota(Long id, String folio, String correoCliente, String estado, Integer total) {}
