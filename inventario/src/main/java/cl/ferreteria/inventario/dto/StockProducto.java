package cl.ferreteria.inventario.dto;

public record StockProducto(
        String codigo,
        String nombre,
        Integer stock,
        Integer stockMinimo,
        boolean stockBajo,
        boolean agotado) {}
