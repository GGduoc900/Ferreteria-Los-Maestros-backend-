package cl.ferreteria.catalogo.dto;

/**
 * Vista pública de un producto. No incluye el precio de compra (dato interno de la ferretería).
 */
public record DetalleProducto(
        String codigo,
        String nombre,
        String marca,
        String categoria,
        String subcategoria,
        String unidad,
        Integer precioVenta,
        String imagen,
        Integer stock,
        Integer stockMinimo,
        boolean disponible,
        String descripcion) {

    public static DetalleProducto de(ProductoInventario p, String descripcion) {
        return new DetalleProducto(
                p.codigo(), p.nombre(), p.marca(), p.categoria(), p.subcategoria(), p.unidad(),
                p.precioVenta(), p.imagen(), p.stock(), p.stockMinimo(),
                p.stock() != null && p.stock() > 0, descripcion);
    }
}
