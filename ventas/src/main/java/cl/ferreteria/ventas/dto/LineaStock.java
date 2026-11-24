package cl.ferreteria.ventas.dto;

/** Respuesta de inventario en /stock/descontar. */
public record LineaStock(String codigo, String nombre, Integer cantidad, Integer precioUnitario, Integer stockRestante) {}
