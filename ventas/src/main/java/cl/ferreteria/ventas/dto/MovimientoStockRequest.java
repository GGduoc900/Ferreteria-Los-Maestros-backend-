package cl.ferreteria.ventas.dto;

import java.util.List;

/** Cuerpo que se envía a inventario en /stock/descontar y /stock/reponer. */
public record MovimientoStockRequest(List<ItemRequest> items) {}
