package cl.ferreteria.inventario.dto;

import java.util.List;

/** Cuerpo de /stock/descontar y /stock/reponer (lo usan ventas y proveedores). */
public record MovimientoStockRequest(List<ItemStockRequest> items) {}
