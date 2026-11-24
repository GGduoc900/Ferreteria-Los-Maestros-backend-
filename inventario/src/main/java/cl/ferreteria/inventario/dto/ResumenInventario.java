package cl.ferreteria.inventario.dto;

public record ResumenInventario(
        long totalProductos, long productosStockBajo, long productosAgotados, long valorInventarioCosto) {}
