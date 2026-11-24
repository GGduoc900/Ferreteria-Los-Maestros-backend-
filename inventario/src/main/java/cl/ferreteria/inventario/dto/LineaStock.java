package cl.ferreteria.inventario.dto;

/** Resultado por producto de un movimiento de stock, con el precio vigente al momento. */
public record LineaStock(
        String codigo, String nombre, Integer cantidad, Integer precioUnitario, Integer stockRestante) {}
